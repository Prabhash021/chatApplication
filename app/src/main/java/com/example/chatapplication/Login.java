package com.example.chatapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {

    SignInButton signIn;
    ProgressBar loading;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signIn = findViewById(R.id.googleBtn);
        loading = findViewById(R.id.loadingPB);
        loading.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(Login.this, options);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // launching the activityResult for signing through google
                Intent intent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(intent);
            }
        });
    }

    public final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()== RESULT_OK){
                loading.setVisibility(View.VISIBLE);
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount googleSignInAccount =  accountTask.getResult(ApiException.class);
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                    auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loading.setVisibility(View.GONE);
                            if(task.isSuccessful()){
                                auth = FirebaseAuth.getInstance();
                                toast("Login successful");
                                markLogin();
                                Intent intent = new Intent(Login.this, EditUserProfile.class);
                                startActivity(intent);
                                finish();
                            }else { toast("Login Failed"); }
                        }
                    });
                } catch (ApiException e) { throw new RuntimeException(e); }
            }
        }
    });

    private void markLogin() {
        //when the login is successful marked login = 1
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("Preference", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("login", 1);
        editor.apply();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}