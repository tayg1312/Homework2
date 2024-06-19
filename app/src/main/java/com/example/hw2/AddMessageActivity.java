package com.example.hw2;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AddMessageActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView addPhoto;
    int REQUEST_PERMISSIONS_CODE = 1;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri currentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_message);

        addPhoto = findViewById(R.id.addPhoto); // Initialize addPhoto here
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                if (currentImage != null) {
                    addPhoto.setImageURI(currentImage);
                }
            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddMessageActivity.this, new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_MEDIA_LOCATION
                    }, REQUEST_PERMISSIONS_CODE);
                } else {
                    captureImage();
                }
            }
        });

        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EditText name = findViewById(R.id.name);
                    EditText text = findViewById(R.id.text);

                    if (name == null || name.getText().toString().isEmpty()) {
                        showNoContentAvailable();
                        return;
                    }

                    String nameText = name.getText().toString();
                    String messageText = (text != null && !text.getText().toString().isEmpty()) ? text.getText().toString() : "";
                    String imageUriString = (currentImage != null) ? currentImage.toString() : "";

                    if (currentImage != null) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference fileRef = storageRef.child(currentImage.getPath());
                        fileRef.putFile(currentImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        saveMessage(uri.toString(), nameText, messageText);
                                    }
                                });
                            }
                        }).addOnFailureListener(e -> showNoContentAvailable());
                    } else {
                        saveMessage(imageUriString, nameText, messageText);
                    }
                } catch (Exception e) {
                    showNoContentAvailable();
                }
            }
        });
    }

    private void saveMessage(String imageUrl, String name, String text) {
        Message c = new Message(imageUrl, name, text);
        db.collection("Messages").document(c.ID).set(c.getAsMap());
        Intent i = new Intent();
        i.putExtra("message", c);
        setResult(1, i);
        finish();
    }

    private void showNoContentAvailable() {
        Toast.makeText(this, "No content available", Toast.LENGTH_SHORT).show();
    }

    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void captureImage() {
        try {
            Uri imageUri = createImageUri();
            if (imageUri != null) {
                currentImage = imageUri;
                takePictureLauncher.launch(imageUri);
            } else {
                showNoContentAvailable();
            }
        } catch (Exception e) {
            showNoContentAvailable();
        }
    }
}