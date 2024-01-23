package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatScreen extends AppCompatActivity {

    TextView chatUserName;
    ListView chat;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        chatUserName = findViewById(R.id.chatUser);

        chatUserName.setText(getChatUserName());




    }

    private String getChatUserName() {
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("Preference", MODE_PRIVATE);
        String userName = sharedPref.getString("chatUser", "User");
        return userName;
    }

}