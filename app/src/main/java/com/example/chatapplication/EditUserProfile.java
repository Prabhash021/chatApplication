package com.example.chatapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class EditUserProfile extends AppCompatActivity {

    String gender;
    Uri imageUri;
    ImageView profileUri;
    EditText name, email, bio, DOB;
    RadioButton male, female;
    Button save, uploadImg;
    ProgressBar loading;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        profileUri = findViewById(R.id.profileImgView);
        name = findViewById(R.id.nameEditTxt);
        email = findViewById(R.id.emailEditTxt);
        bio = findViewById(R.id.bioEditTxt);
        DOB = findViewById(R.id.dateEditTxt);
        male = findViewById(R.id.maleRadioBtn);
        female = findViewById(R.id.femaleRadioBtn);
        save = findViewById(R.id.saveBtn);
        uploadImg = findViewById(R.id.uploadImg);
        loading = findViewById(R.id.loadingPB);

        email.setEnabled(false);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        checkData();

        male.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                gender = "Male";
            }
        });
        female.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                gender = "Female";
            }
        });
        save.setOnClickListener(v -> saveDetails());

        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        imageUri = uri;
                        Glide.with(EditUserProfile.this)
                                .load(uri)
                                .into(profileUri);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        profileUri.setOnClickListener(v -> {
            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        uploadImg.setOnClickListener(v -> uploadImg(imageUri));
    }

    private void saveDetails() {
        String uName = name.getText().toString();
        String uEmail = email.getText().toString();
        String uBio = bio.getText().toString();
        String uDob = DOB.getText().toString();
        String uGender = gender;
        Uri uProfileUri = imageUri;
        String uId = auth.getCurrentUser().getUid();

        if(uName.trim().isEmpty() || uEmail.trim().isEmpty() || uBio.trim().isEmpty() || uDob.trim().isEmpty() || uGender.trim().isEmpty()){
            Toast.makeText(EditUserProfile.this, "Please provide all the details", Toast.LENGTH_SHORT).show();
        }
        else {
            UserDataModel userDataModel = new UserDataModel(uName, uEmail, uBio, uDob, uGender,uProfileUri);

            db.collection("UserData").document(uId).set(userDataModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Intent intent = new Intent(EditUserProfile.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditUserProfile.this, "Unable to save data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadImg(Uri imageUri) {
        if(imageUri!=null){
            StorageReference ref = storageReference.child("images/" + Objects.requireNonNull(auth.getCurrentUser()).getUid());
            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(EditUserProfile.this, "Image Saved", Toast.LENGTH_SHORT).show();
                    String imageUrl = taskSnapshot.getStorage().getPath();
                    Log.e("StoragePath", "storagrUrl > "+ imageUrl);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditUserProfile.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FailedImage", "error image upload > "+ e.getMessage());
                }
            });
        }
        else {
            Toast.makeText(this, "Select new image", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkData(){
        loading.setVisibility(View.VISIBLE);
        DocumentReference docRef = db.collection("UserData").document(Objects.requireNonNull(auth.getCurrentUser()).getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    loading.setVisibility(View.GONE);
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Log.e("Check Doc", "On success, Checking document > " + document.getData());
                        name.setText(document.getString("name"));
                        bio.setText(document.getString("bio"));
                        String gender = document.getString("gender");
                        if(Objects.equals(gender, "Male")){
                            male.setChecked(true);
                        }else {
                            female.setChecked(true);
                        }
                        DOB.setText(document.getString("dob"));
                        imageUri = Uri.parse(document.getString("profileUri"));
                        Glide.with(EditUserProfile.this)
                                .load(imageUri)
                                .into(profileUri);
                    }
                    else {
                        Log.e("Check Doc", "On Success, Document doesn't exists");
                        name.setText(Objects.requireNonNull(auth.getCurrentUser()).getDisplayName());
                        email.setText(auth.getCurrentUser().getEmail());
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Check Doc", "On failed, error > "+ e.getMessage());
            }
        });
    }
}