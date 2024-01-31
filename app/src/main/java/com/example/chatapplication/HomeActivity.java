package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    public final String TAG = "HomeActivity";
    TextView profile;
    ProgressBar loading;
    HomeScreenAdapter adapter;
    RecyclerView recyclerView;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        profile = findViewById(R.id.profileView);
        loading = findViewById(R.id.loadingHome);
        recyclerView = findViewById(R.id.userListRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<UserDataModel> arrayList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        loading.setVisibility(View.VISIBLE);

        db.collection("UserData").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    loading.setVisibility(View.GONE);
                    for(QueryDocumentSnapshot document : task.getResult()){
                        String userName = document.getString("name");
                        String userId = document.getString("userId");
                        String userMail = document.getString("email");
                        Uri profileLink = Uri.parse(document.getString("profileUri"));

//                        Log.e(TAG, "Get users name -> "+userName);
//                        Log.e(TAG, "Get users email -> "+userMail);

                        arrayList.add(new UserDataModel(userName, userId, profileLink, userMail));
                    }
                    adapter = new HomeScreenAdapter(HomeActivity.this, arrayList);
                    recyclerView.setAdapter(adapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HomeActivity.this, "failed call", Toast.LENGTH_SHORT).show();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, UserProfile.class);
                startActivity(intent);
            }
        });
    }
}