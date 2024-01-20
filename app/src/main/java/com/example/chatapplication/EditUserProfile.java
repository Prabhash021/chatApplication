package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

public class EditUserProfile extends AppCompatActivity {

    ImageView profileUri;
    EditText name, email, bio, DOB;
    RadioButton male, female;
    Button save;

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



    }
}