package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;

import java.util.ArrayList;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    public final String TAG = "HomeActivity";
    HomeScreenAdapter adapter;
    RecyclerView recyclerView;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        recyclerView = findViewById(R.id.userListRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        auth = FirebaseAuth.getInstance();

        ArrayList<UserDataModel> arrayList = new ArrayList<>();

        Log.e(TAG, "Provider -> "+ auth.getCurrentUser().getProviderData());

        for(UserInfo profile : Objects.requireNonNull(auth.getCurrentUser()).getProviderData()){
            Log.e(TAG, "Get users name -> "+Objects.requireNonNull(profile.getDisplayName()));
            Log.e(TAG, "Get users email -> "+Objects.requireNonNull(profile.getEmail()));

            String userName = profile.getDisplayName();
            String UserMail = profile.getEmail();
            Uri imageLink = profile.getPhotoUrl();

            arrayList.add(new UserDataModel(userName, UserMail, imageLink));

        }
        adapter = new HomeScreenAdapter(this, arrayList);
        recyclerView.setAdapter(adapter);

    }
}