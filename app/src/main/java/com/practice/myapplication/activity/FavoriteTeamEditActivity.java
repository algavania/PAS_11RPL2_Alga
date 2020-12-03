package com.practice.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.practice.myapplication.R;
import com.practice.myapplication.RealmHelper;
import com.practice.myapplication.model.Preferences;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class FavoriteTeamEditActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    Bundle extras;
    String team, imageUrl, description, formedYear, stadiumName, formerDescription;
    TextInputEditText txt_name, txt_stadium, txt_desc, txt_year;
    ImageView img_edit;
    Button btn_edit;

    final int IMG_REQUEST = 1000;
    ProgressDialog progressDialog;
    Uri imageUri, downloadUri;

    Realm realm;
    RealmHelper realmHelper;

    FirebaseStorage storage;
    StorageReference storageReference;
    StorageReference fileReference;

    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_team_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, permissions)) {
            EasyPermissions.requestPermissions(this, "Grant the permission to fully access this apps", 10, permissions);
        }

        preferences = new Preferences();
        if (preferences.getDominantColor(getApplicationContext()) != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(preferences.getDominantColor(getApplicationContext()));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(preferences.getDominantColor(getApplicationContext())));
        }

        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder().allowWritesOnUiThread(true).build();
        realm = Realm.getInstance(configuration);
        realmHelper = new RealmHelper(realm);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("teams");

        progressDialog = new ProgressDialog(this);

        img_edit = findViewById(R.id.img_team_edit);
        txt_name = findViewById(R.id.txt_edit_team_name);
        txt_stadium = findViewById(R.id.txt_edit_team_stadium);
        txt_desc = findViewById(R.id.txt_edit_team_description);
        txt_year = findViewById(R.id.txt_edit_team_year);
        btn_edit = findViewById(R.id.btn_edit_team);

        extras = getIntent().getExtras();
        if (extras != null) {
            team = extras.getString("team");
            imageUrl = extras.getString("imageUrl");
            formerDescription = extras.getString("description");
            formedYear = extras.getString("year");
            stadiumName = extras.getString("stadium");

            getSupportActionBar().setTitle(team);
            txt_name.setText(team);
            txt_desc.setText(formerDescription);
            txt_stadium.setText(stadiumName);
            txt_year.setText(formedYear);
            Glide.with(getApplicationContext()).load(imageUrl)
                    .placeholder(R.drawable.icon)
                    .fitCenter()
                    .into(img_edit);
        }

        img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
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
        team = txt_name.getText().toString();
        stadiumName = txt_stadium.getText().toString();
        formedYear = txt_year.getText().toString();
        description = txt_desc.getText().toString();
        if (team.isEmpty() || stadiumName.isEmpty() || formedYear.length() == 0 ||
                description.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fill the data", Toast.LENGTH_SHORT).show();
        } else {
            if (imageUri != null) {
                progressDialog.setMessage("Updating your changes...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
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
                            realmHelper.updateTeam(formerDescription, imageUrl, team, description, formedYear, stadiumName);
                            Toast.makeText(getApplicationContext(), "Edit successful", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            goToFavorite();
                        } else {
                            Toast.makeText(getApplicationContext(), "Edit failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                realmHelper.updateTeam(formerDescription, imageUrl, team, description, formedYear, stadiumName);
                Toast.makeText(getApplicationContext(), "Edit successful", Toast.LENGTH_SHORT).show();
                goToFavorite();
            }

        }
    }

    private void goToFavorite() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("fromFavorite", true);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageUrl = data.getDataString();
            Glide.with(getApplicationContext()).load(imageUri).fitCenter().into(img_edit);
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

    // Request Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
}