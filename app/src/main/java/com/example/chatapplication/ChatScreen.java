package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatScreen extends AppCompatActivity {

    TextView chatUserNameTV;
    ListView chat;
    EditText msg;
    Button send, refresh;
    ProgressBar loading;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    String chatId;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        chatUserNameTV = findViewById(R.id.chatUser);
        msg = findViewById(R.id.chatMsg);
        send = findViewById(R.id.sendMsg);
        refresh = findViewById(R.id.refreshBtn);
        chat = findViewById(R.id.chatLV);
        loading = findViewById(R.id.loadingCS);

        arrayList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);

        chat.setAdapter(adapter);

        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("Preference", MODE_PRIVATE);
        String chatUserName = sharedPref.getString("chatUser", "User");
        String chatUserMail = sharedPref.getString("chatUserId", null);

        chatUserNameTV.setText(chatUserName);

        String senderId = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
        String chatId1 = senderId + chatUserMail;
        String chatId2 = chatUserMail + senderId;

        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());

        chatId = checkChatId(chatId1, chatId2);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMsg = msg.getText().toString();
                if(!newMsg.isEmpty()){

                    msgModel model = new msgModel(auth.getCurrentUser().getDisplayName(), newMsg, timeStamp);
                    db.collection("ChatData").document(chatId).collection(chatId).document(timeStamp).set(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            arrayList.add(newMsg);
                            adapter.notifyDataSetChanged();
                            msg.setText("");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("ChatData", "Chat data failed to add > " + e.getMessage());
                        }
                    });
                }
            }
        });

        DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child("ChatData").child(chatId);
        Log.e("Check DbRef", String.valueOf(databaseReference));
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Log.e("onDataChange", "value > "+ dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OnCancelled", "error > "+error.getMessage());
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRestart();
            }
        });
    }

    private String checkChatId(String chatId1, String chatId2){
        loading.setVisibility(View.VISIBLE);
        DocumentReference documentReference = db.collection("ChatData").document(chatId1);
        documentReference.collection(chatId1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                loading.setVisibility(View.GONE);
                if(!queryDocumentSnapshots.isEmpty()){
                    chatId = chatId1;
                }
                getChatData(chatId);
                send.setEnabled(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatScreen.this, "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });
        if(chatId == null){
            chatId = chatId2;
        }
        return chatId;
    }

    private void getChatData(String chatId) {
        DocumentReference documentReference = db.collection("ChatData").document(chatId);
        documentReference.collection(chatId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful() && !task.getResult().isEmpty()){
                    for(QueryDocumentSnapshot chat: task.getResult()){
                        String msg = chat.getString("msg");
//                        Log.e("getChat", "OnComplete of getting chat > "+ msg);
                        arrayList.add(msg);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatScreen.this, "Unable to get Data", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void onRestart(){
        super.onRestart();

        Intent intent = new Intent(ChatScreen.this, ChatScreen.class);
        startActivity(intent);
        finish();
    }

    public void onStart(){
        super.onStart();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        db.collection("ChatData").document(chatId).collection(chatId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Log.e("SnapshotListener", "error get in snapshot listener > "+ error.getMessage());
                    return;
                }
                if(value != null){
                    /*for(QueryDocumentSnapshot documentChange : value){
                        Log.e("DataChange", "new data > "+ documentChange.getString("msg"));
                        arrayList.add(documentChange.getString("msg"));
                    }*/
                    for(DocumentChange dc: value.getDocumentChanges()){
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Log.e("newEntry", "new data > " + dc.getDocument().getString("msg"));
                        }
                    }
                }
            }
        });
    }
}