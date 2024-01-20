package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    public static final int SplashTimeOut = 167;

    ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPref = getBaseContext().getSharedPreferences("Preference", MODE_PRIVATE);
                int checkLogin = sharedPref.getInt("login", 0);
                Log.e("Splash","Value of checkLogin > "+ checkLogin);

                Intent intent;
                if(checkLogin == 1){
                    // checks user is still logged in then redirected toward HomeActivity screen
                    intent = new Intent(SplashScreen.this, HomeActivity.class);
                } else {
                    // if user is not logged in then redirected towards the login screen
                    intent = new Intent(SplashScreen.this, Login.class);
                }
                startActivity(intent);
                finish();
            }
        }, SplashTimeOut);
    }
}