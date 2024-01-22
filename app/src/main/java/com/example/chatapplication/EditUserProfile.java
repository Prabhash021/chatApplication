package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class EditUserProfile extends AppCompatActivity {

    ImageView profileUri;
    EditText name, email, bio, DOB;
    String gender;
    RadioButton male, female;
    Button save;
    ProgressBar loading;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db;

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
        loading = findViewById(R.id.loadingPB);

        email.setEnabled(false);

        db = FirebaseFirestore.getInstance();

        Glide.with(EditUserProfile.this)
                .load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl())
                .into(profileUri);
        name.setText(auth.getCurrentUser().getDisplayName());
        email.setText(auth.getCurrentUser().getEmail());

        male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    gender = "Male";
                }
            }
        });
        female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    gender = "Female";
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDetails();
            }
        });



    }

    private void saveDetails() {
        String uName = name.getText().toString();
        String uEmail = email.getText().toString();
        String uBio = bio.getText().toString();
        String uDob = DOB.getText().toString();
        String uGender = gender;
        Uri uProfileUri = (Objects.requireNonNull(auth.getCurrentUser())).getPhotoUrl();
        String uId = auth.getCurrentUser().getUid();

        if(uName.trim().isEmpty() || uEmail.trim().isEmpty() || uBio.trim().isEmpty() || uDob.trim().isEmpty() || uGender.trim().isEmpty()){
            Toast.makeText(EditUserProfile.this, "Please provide all the details", Toast.LENGTH_SHORT).show();
        }
        else {
            UserDataModel userDataModel = new UserDataModel(uName, uEmail, uBio, uDob, uGender,uProfileUri);
            /*db.collection("UserData").add(userDataModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(EditUserProfile.this, "success", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditUserProfile.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });*/

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
}