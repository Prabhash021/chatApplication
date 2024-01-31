package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Objects;

public class UserProfile extends AppCompatActivity {

    CircularImageView circularImageView;
    ProgressBar pbLoader;
    TextView bio, name, email, dob;
    Button editProfile;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        circularImageView = findViewById(R.id.IV_ProfilePicture);
        bio = findViewById(R.id.TV_bio);
        name = findViewById(R.id.TV_name);
        email = findViewById(R.id.TV_email);
        dob = findViewById(R.id.TV_dob);
        editProfile = findViewById(R.id.Btn_editProfile);
        pbLoader = findViewById(R.id.PB_loader);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        pbLoader.setVisibility(View.VISIBLE);
        editProfile.setEnabled(false);

        DocumentReference docRef = db.collection("UserData").document(Objects.requireNonNull(auth.getCurrentUser()).getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    pbLoader.setVisibility(View.GONE);
                    editProfile.setEnabled(true);
                    if(document.exists()){
                        name.setText(document.getString("name"));
                        bio.setText(document.getString("bio"));
                        email.setText(document.getString("email"));
                        dob.setText(document.getString("dob"));
                        Uri imageUri = Uri.parse(document.getString("profileUri"));
                        Glide.with(UserProfile.this)
                                .load(imageUri)
                                .into(circularImageView);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pbLoader.setVisibility(View.GONE);
                Toast.makeText(UserProfile.this, "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, EditUserProfile.class);
                startActivity(intent);
            }
        });
    }
}