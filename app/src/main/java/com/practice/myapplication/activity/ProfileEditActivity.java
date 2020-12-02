package com.practice.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.practice.myapplication.R;
import com.practice.myapplication.model.UploadProfile;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ProfileEditActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    Bundle extras;
    String id, name, email, imageUrl, media, location, about;
    TextInputEditText txt_name, txt_email, txt_description, txt_location;
    CircularImageView img_profile;
    Button btn_submit;

    DatabaseReference databaseReference;
    FirebaseStorage storage;
    StorageReference storageReference;
    StorageReference fileReference;

    final int IMG_REQUEST = 1000;
    ProgressDialog progressDialog;
    Uri imageUri, downloadUri;

    FirebaseUser user;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        getSupportActionBar().show();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        progressDialog = new ProgressDialog(this);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("users");
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        final String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, permissions)) {
            EasyPermissions.requestPermissions(this, "Grant the permission to fully access this apps", 10, permissions);
        }

        txt_name = findViewById(R.id.txt_edit_profile_name);
        txt_email = findViewById(R.id.txt_edit_profile_email);
        txt_description = findViewById(R.id.txt_edit_profile_description);
        txt_location = findViewById(R.id.txt_edit_profile_location);
        img_profile = findViewById(R.id.img_profile_edit);
        btn_submit = findViewById(R.id.btn_submit_profile);

        extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getString("id");
            name = extras.getString("name");
            email = extras.getString("email");
            imageUrl = extras.getString("image");
            media = extras.getString("media");
            location = extras.getString("location");
            about = extras.getString("about");

            if (email.trim().length() != 0) {
                txt_email.setText(email);
            }
            txt_name.setText(name);
            txt_location.setText(location);
            txt_description.setText(about);
            Glide.with(getApplicationContext()).load(imageUrl)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .fitCenter()
                    .into(img_profile);
        }

        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editData();
            }
        });
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void editData() {
        name = txt_name.getText().toString();
        email = txt_email.getText().toString();
        location = txt_location.getText().toString();
        about = txt_description.getText().toString();
        if (name.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || location.isEmpty() || about.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fill the data correctly", Toast.LENGTH_SHORT).show();
        } else {
            if (imageUri != null) {
                progressDialog.setMessage("Updating your profile...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                fileReference = storageReference.child(media).child(id).child("profile." + getFileExtension(imageUri));
                fileReference.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return fileReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadUri = task.getResult();
                            imageUrl = downloadUri.toString();

                            UploadProfile uploadProfile = new UploadProfile(name, email, about, location, imageUrl);
                            databaseReference.child(media).child(id).setValue(uploadProfile);

                            updateProfile();
                        } else {
                            Toast.makeText(getApplicationContext(), "Edit failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                updateProfile();
            }

        }
    }

    private void updateProfile() {
        Log.d("email", email);
        UploadProfile uploadProfile = new UploadProfile(name, email, about, location, imageUrl);
        databaseReference.child(media).child(id).setValue(uploadProfile);
        if (user != null) {
            Log.d("user", "not null");
            user.updateEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Edit successful", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                goToProfile();
                                Log.d("TAG", "User email address updated.");
                            }
                        }
                    });
        } else {
            Log.d("user", "null");
            Toast.makeText(getApplicationContext(), "Edit successful", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            goToProfile();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageUrl = data.getDataString();
            Glide.with(getApplicationContext()).load(imageUri).fitCenter().into(img_profile);
        }
    }


    // Request Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToProfile() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("fromProfile", true);
        startActivity(intent);
        finish();
    }
}