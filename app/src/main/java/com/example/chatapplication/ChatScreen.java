package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatScreen extends AppCompatActivity {

    TextView chatUserNameTV;
    ListView chat;
    EditText msg;
    Button send;
    ProgressBar loading;
    SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<String> chatList;
    ArrayAdapter<String> adapter;
    String chatId;
    FirebaseAuth auth;
    FirebaseFirestore db;
    DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        chatUserNameTV = findViewById(R.id.chatUser);
        msg = findViewById(R.id.chatMsg);
        send = findViewById(R.id.sendMsg);
        chat = findViewById(R.id.chatLV);
        loading = findViewById(R.id.loadingCS);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        chatList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatList); //adapter for chatList

        chat.setAdapter(adapter);

        //details of another user in chat
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("Preference", MODE_PRIVATE);
        String chatUserName = sharedPref.getString("chatUser", "User");
        String chatUserId = sharedPref.getString("chatUserId", null);
        /*Log.e("ChatId", "chatUserId > " + chatUserId);*/

        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());

        //name of another chat user
        chatUserNameTV.setText(chatUserName);

        //possible chatId's for storing chat data
        String senderId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        String chatId1 = senderId + chatUserId;
        String chatId2 = chatUserId + senderId;

        //is there any chatId already existed from these possibilities, if not then chatId2 will be set as chatId
        chatId = checkChatId(chatId1, chatId2);
        Log.e("chatId", "current Chat Id: "+ chatId);

        //reference for the chatId document section
        documentReference = db.collection("ChatData").document(chatId);
        //changes happens in document. addSnapshotListener, starts listening to the document referenced by above DocumentReference.
        /*documentReference.collection(chatId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if( value != null && !value.isEmpty()){
                    int i = value.getDocumentChanges().size();
                    Log.e("QuerySnapshot", "data > "+ value.getDocumentChanges().get(--i).getDocument().getString("msg"));
                    //selecting the last document of the chat
                    String msg = value.getDocumentChanges().get(i).getDocument().getString("msg");
                    chatList.add(msg);
                    adapter.notifyDataSetChanged();
                   *//* getChatData(chatId);*//*
                }
            }
        });*/

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                assert value != null;
                if(value.exists()){

                    ArrayList<HashMap<String, String>> users = (ArrayList<HashMap<String, String>>) value.get("Message");
                    assert users != null;
                    Log.e("ChatData", "Chat > "+ users.size());

                    for(int i = 0 ; i<users.size(); i++){
                        HashMap<String, String> chat = users.get(i);
                        String msg = chat.get("msg");
                        chatList.add(msg);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        //swipe down to refresh the chatList
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chatList.clear();

                getChatData(chatId);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // send the message
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //store the message to string from EditText field
                String newMsg = msg.getText().toString();
                if(!newMsg.isEmpty()){
                    msgModel model = new msgModel(auth.getCurrentUser().getDisplayName(), newMsg, timeStamp);
                    //store to the fireStore db,
                    /*db.collection("ChatData").document(chatId).collection(chatId).document(timeStamp).set(model)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // on success message is added to the chatList, here notifyDataSetChanged() not used as in line-96 there already addSnapshotListener is in use
                            chatList.add(newMsg);
                            msg.setText("");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("ChatData", "Chat data failed to add > " + e.getMessage());
                        }
                    });*/

                    documentReference.update("Message", FieldValue.arrayUnion(model)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            chatList.add(newMsg);
                            msg.setText("");
                            Toast.makeText(ChatScreen.this, "Data added to db", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            checkDocument(model, documentReference);
                            Toast.makeText(ChatScreen.this, "First msg", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    private void checkDocument(msgModel model, DocumentReference documentReference) {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(!document.exists()){
                        onFirstMsg(model, documentReference);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatScreen.this, "Failed to check", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onFirstMsg(msgModel model, DocumentReference documentReference) {
        Map<String, Object> newMsg = new HashMap<>();
        newMsg.put("Message", Collections.singletonList(model));
        documentReference.set(newMsg).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                msg.setText("");
                Toast.makeText(ChatScreen.this, "Data Added", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatScreen.this, "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //checks is there any chatId already existed or not if not then chatId2 will be the chatId
    private String checkChatId(String chatId1, String chatId2){
        loading.setVisibility(View.VISIBLE);
        DocumentReference documentReference = db.collection("ChatData").document(chatId1);
        /*documentReference.collection(chatId1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                loading.setVisibility(View.GONE);
                if(!queryDocumentSnapshots.isEmpty()){
                    chatId = chatId1;
                }
                chatList.clear();
                getChatData(chatId);
                send.setEnabled(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loading.setVisibility(View.GONE);
                Toast.makeText(ChatScreen.this, "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });*/

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                loading.setVisibility(View.GONE);
                if(!documentSnapshot.exists()){
                    chatId = chatId1;
                }
                chatList.clear();
                getChatData(chatId);
                send.setEnabled(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loading.setVisibility(View.GONE);
                Toast.makeText(ChatScreen.this, "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });
        if(chatId == null){
            chatId = chatId2;
        }
        return chatId;
    }

    //starts getting the entire chat data with chatId
    private void getChatData(String chatId) {
        DocumentReference documentReference = db.collection("ChatData").document(chatId);
        /*documentReference.collection(chatId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful() && !task.getResult().isEmpty()){
                    for(QueryDocumentSnapshot chat: task.getResult()){
                        String msg = chat.getString("msg");
                        String userName = chat.getString("userId");
                        *//*Log.e("getChat", "OnComplete of getting chat > "+ msg);*//*
                        chatList.add(userName + "\n" + msg +"\n\n");
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatScreen.this, "Unable to get Data", Toast.LENGTH_SHORT).show();
            }
        });*/
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()){

                    ArrayList<HashMap<String, String>> users = (ArrayList<HashMap<String, String>>) document.get("Message");
                    assert users != null;
                    Log.e("ChatData", "Chat > "+ users.size());

                    for(int i = 0 ; i<users.size(); i++){
                        HashMap<String, String> chat = users.get(i);
                        String msg = chat.get("msg");
                        String userName = chat.get("userId");
                        chatList.add(userName + "\n" + msg +"\n\n");
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}