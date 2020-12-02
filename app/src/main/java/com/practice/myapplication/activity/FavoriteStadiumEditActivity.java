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

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class FavoriteStadiumEditActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    Bundle extras;
    String stadiumName, stadiumDesc, stadiumImage, stadiumLocation, formerDescription;
    ImageView img_edit;
    TextInputEditText txt_name, txt_location, txt_desc;
    Button btn_edit;

    final int IMG_REQUEST = 1000;
    ProgressDialog progressDialog;
    Uri imageUri, downloadUri;

    Realm realm;
    RealmHelper realmHelper;

    FirebaseStorage storage;
    StorageReference storageReference;
    StorageReference fileReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_stadium_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, permissions)) {
            EasyPermissions.requestPermissions(this, "Grant the permission to fully access this apps", 10, permissions);
        }

        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder().allowWritesOnUiThread(true).build();
        realm = Realm.getInstance(configuration);
        realmHelper = new RealmHelper(realm);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("stadiums");

        progressDialog = new ProgressDialog(this);

        img_edit = findViewById(R.id.img_stadium_edit);
        txt_name = findViewById(R.id.txt_edit_stadium_name);
        txt_location = findViewById(R.id.txt_edit_stadium_location);
        txt_desc = findViewById(R.id.txt_edit_stadium_description);
        btn_edit = findViewById(R.id.btn_edit_stadium);

        extras = getIntent().getExtras();
        if (extras != null) {
            stadiumName = extras.getString("stadium");
            formerDescription = extras.getString("stadiumDesc");
            stadiumImage = extras.getString("stadiumImage");
            stadiumLocation = extras.getString("stadiumLocation");

            getSupportActionBar().setTitle(stadiumName);

            txt_name.setText(stadiumName);
            txt_desc.setText(formerDescription);
            txt_location.setText(stadiumLocation);
            Glide.with(getApplicationContext()).load(stadiumImage)
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
        stadiumName = txt_name.getText().toString();
        stadiumLocation = txt_location.getText().toString();
        stadiumDesc = txt_desc.getText().toString();
        if (stadiumName.isEmpty() || stadiumLocation.isEmpty() || stadiumDesc.isEmpty()) {
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
                            stadiumImage = downloadUri.toString();
                            realmHelper.updateStadium(formerDescription, stadiumImage, stadiumName, stadiumDesc, stadiumLocation);
                            Toast.makeText(getApplicationContext(), "Edit successful", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            goToFavorite();
                        } else {
                            Toast.makeText(getApplicationContext(), "Edit failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                realmHelper.updateStadium(formerDescription, stadiumImage, stadiumName, stadiumDesc, stadiumLocation);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            stadiumImage = data.getDataString();
            Glide.with(getApplicationContext()).load(imageUri).fitCenter().into(img_edit);
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
}