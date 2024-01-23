package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatScreen extends AppCompatActivity {

    TextView chatUserName;
    ListView chat;
    EditText msg;
    Button send;
    ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        chatUserName = findViewById(R.id.chatUser);
        msg = findViewById(R.id.chatMsg);
        send = findViewById(R.id.sendMsg);
        chat = findViewById(R.id.chatLV);

        chatUserName.setText(getChatUserName());

        arrayList = new ArrayList<>();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);

        chat.setAdapter(adapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMsg = msg.getText().toString();
                if(!newMsg.isEmpty()){
                    arrayList.add(newMsg);
                    adapter.notifyDataSetChanged();
                    msg.setText("");
                }
            }
        });
    }

    private String getChatUserName() {
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("Preference", MODE_PRIVATE);
        String userName = sharedPref.getString("chatUser", "User");
        return userName;
    }

}