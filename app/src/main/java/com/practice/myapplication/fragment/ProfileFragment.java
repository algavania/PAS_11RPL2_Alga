package com.practice.myapplication.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.practice.myapplication.R;
import com.practice.myapplication.activity.LoginActivity;
import com.practice.myapplication.activity.ProfileEditActivity;
import com.practice.myapplication.model.Preferences;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    CircularImageView img_profile;
    String name, imageProfileUrl, email, loginMedia, userId, location, about;
    TextView tv_name, tv_email, tv_location, tv_description;
    Button btn_logout, btn_edit;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    AccessToken accessToken;
    GoogleSignInAccount account;
    GoogleSignInClient mGoogleSignInClient;
    Boolean isLoggedIn;
    Preferences preferences;
    ProgressDialog progressDialog;

    DatabaseReference databaseReference;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setShowHideAnimationEnabled(false);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_name = view.findViewById(R.id.tv_name_profile);
        img_profile = view.findViewById(R.id.img_profile);
        btn_edit = view.findViewById(R.id.btn_edit_profile);
        btn_logout = view.findViewById(R.id.btn_logout);
        tv_email = view.findViewById(R.id.tv_email_profile);
        tv_description = view.findViewById(R.id.tv_description_profile);
        tv_location = view.findViewById(R.id.tv_location_profile);

        accessToken = AccessToken.getCurrentAccessToken();
        isLoggedIn = accessToken != null && !accessToken.isExpired();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        progressDialog = new ProgressDialog(getActivity());

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        account = GoogleSignIn.getLastSignedInAccount(getActivity());

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        preferences = new Preferences();

        progressDialog.setMessage("Loading your profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (account != null) {
            userId = account.getId();
            name = account.getDisplayName();
            email = account.getEmail();
            imageProfileUrl = account.getPhotoUrl().toString();

            Log.d("image", ""+imageProfileUrl);
            if (isAdded()) {
                Glide.with(getActivity()).load(imageProfileUrl)
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .fitCenter()
                        .into(img_profile);
            }
            tv_name.setText(name);
            tv_email.setText(email);
            loginMedia = "google";
            getAllProfileInfo();
        } else if (user != null) {
            userId = user.getUid();
            loginMedia = "firebase";
            email = user.getEmail();
            if (user.getDisplayName() != null) {
                name = user.getDisplayName();
                Log.d("name", ""+name);
                tv_name.setText(name);

            }
            tv_email.setText(email);
            if (user.getPhotoUrl() != null) {
                imageProfileUrl = user.getPhotoUrl().toString();

                Log.d("image", ""+imageProfileUrl);
                if (isAdded()) {
                    Glide.with(getActivity()).load(imageProfileUrl)
                            .placeholder(R.drawable.ic_baseline_person_24)
                            .fitCenter()
                            .into(img_profile);
                }
            }
            getAllProfileInfo();
        } else if (isLoggedIn) {
            loginMedia = "facebook";
            facebookLogin();
        }

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (account != null) {
                    signOut();
                } else if (user != null) {
                    firebaseAuth.signOut();
                    checkUserStatus();
                } else if (isLoggedIn) {
                    LoginManager.getInstance().logOut();
                    checkUserStatus();
                }
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
                intent.putExtra("id", userId);
                intent.putExtra("name", name);
                intent.putExtra("location", tv_location.getText().toString());
                intent.putExtra("about", tv_description.getText().toString());
                intent.putExtra("email", email);
                intent.putExtra("image", imageProfileUrl);
                intent.putExtra("media", loginMedia);
                getActivity().startActivity(intent);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    private void checkUserStatus() {
        preferences.setStatus(getActivity(), false);
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    private void signOut() {
        progressDialog.setMessage("Signing you out...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        preferences.setStatus(getActivity(), false);
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
                        progressDialog.dismiss();
                    }
                });
    }


    private void facebookLogin() {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code
                        if (object != null) {
                            Log.d("test", object.toString());
                            try {
                                userId = object.getString("id");
                                name = object.getString("name");
                                email = object.getString("email");
                                tv_email.setText(email);
                                tv_name.setText(name);
                                if (object.has("picture")) {
                                    imageProfileUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");

                                    Log.d("image", ""+imageProfileUrl);
                                    if (isAdded()) {
                                        Glide.with(getActivity()).load(imageProfileUrl)
                                                .placeholder(R.drawable.ic_baseline_person_24)
                                                .fitCenter()
                                                .into(img_profile);
                                    }
                                }
                                getAllProfileInfo();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d("test", "null");
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,name,cover,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void getProfileInfo(DatabaseReference reference, TextView tv_info, String text) {
        if (reference != null) {
            progressDialog.setMessage("Loading your profile...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            Log.d("firebase", "info");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String info = snapshot.getValue(String.class);
                    if (info != null) {
                        if (!info.trim().equals("")){
                            tv_info.setText(info);
                            if (text.equals("name")) {
                                name = info;
                            } else if (text.equals("about")) {
                                about = info;
                            } else if (text.equals("email")) {
                                email = info;
                            } else if (text.equals("location")) {
                                location = info;
                            }
                        }
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            progressDialog.dismiss();
        }
    }

    private void getImageInfo(DatabaseReference reference) {
        if (reference != null) {
            Log.d("image", "firebase");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String info = snapshot.getValue(String.class);
                    if (info != null) {
                        if (!info.trim().equals("")) {
                            imageProfileUrl = info;
                            if (isAdded()) {
                                Glide.with(getActivity()).load(imageProfileUrl)
                                        .placeholder(R.drawable.ic_baseline_person_24)
                                        .fitCenter()
                                        .into(img_profile);
                            }

                            Log.d("image", ""+imageProfileUrl);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void getAllProfileInfo() {
        DatabaseReference dataName = databaseReference.child(loginMedia).child(userId).child("name");
        getProfileInfo(dataName, tv_name, "name");
        DatabaseReference dataEmail = databaseReference.child(loginMedia).child(userId).child("email");
        getProfileInfo(dataEmail, tv_email, "email");
        DatabaseReference dataLocation = databaseReference.child(loginMedia).child(userId).child("location");
        getProfileInfo(dataLocation, tv_location, "location");
        DatabaseReference dataAbout = databaseReference.child(loginMedia).child(userId).child("about");
        getProfileInfo(dataAbout, tv_description, "about");
        DatabaseReference dataImage = databaseReference.child(loginMedia).child(userId).child("image");
        getImageInfo(dataImage);
    }

}