package com.practice.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.practice.myapplication.R;
import com.practice.myapplication.model.Preferences;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText txt_login_username, txt_login_password;
    TextInputLayout txt_layout_username, txt_layout_password;
    Button btn_login;
    TextView tv_register;
    String email, password;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    Preferences preferences;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInButton btn_google;
    final int RC_SIGN_IN = 101;
    CallbackManager callbackManager;
    LoginButton loginButton;
    private static final String EMAIL = "email";
    AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        txt_login_username = findViewById(R.id.txt_login_username);
        txt_layout_username = findViewById(R.id.login_layout_username);

        txt_login_password = findViewById(R.id.txt_login_password);
        txt_layout_password = findViewById(R.id.login_layout_password);

        btn_login = findViewById(R.id.btn_login);
        tv_register = findViewById(R.id.tv_register);
        btn_google = findViewById(R.id.btn_google);

        callbackManager = CallbackManager.Factory.create();

        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));
        // If you are using in a fragment, call loginButton.setFragment(this);

        accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if (isLoggedIn) {
            facebookLogin();
        }

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookLogin();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        preferences = new Preferences();

        Log.d("Pref: ", "" + preferences.getStatus(getApplicationContext()));

        if (preferences.getStatus(getApplicationContext()) || account != null) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null || account != null) {
            preferences.setStatus(getApplicationContext(), true);
        }
        progressDialog = new ProgressDialog(this);

        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        txt_login_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    loginUser();
                }
                return false;
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
        btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInGoogle();
            }
        });
    }

    private void facebookLogin() {
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        finish();
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void loginUser() {
        // Email: test@gmail.com
        // Password: password
        email = txt_login_username.getText().toString();
        password = txt_login_password.getText().toString();
        if (password.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txt_layout_username.setError("Invalid email");
            txt_layout_password.setError("Invalid password");
        } else if (Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.isEmpty()) {
            txt_layout_password.setError("Invalid password");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txt_layout_username.setError("Invalid email");
        } else if (password.length() < 6) {
            txt_layout_password.setError("Password must be at least 6 characters");
        } else {
            txt_layout_username.setError(null);
            txt_layout_password.setError(null);
            progressDialog.setMessage("Signing you in...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                preferences.setStatus(getApplicationContext(), true);
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d("acc", account.toString());

            // Signed in successfully, show authenticated UI.
            if (account != null) {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                progressDialog.dismiss();
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Failed", "signInResult:failed code=" + e.getStatusCode());
            progressDialog.dismiss();
        }
    }

}