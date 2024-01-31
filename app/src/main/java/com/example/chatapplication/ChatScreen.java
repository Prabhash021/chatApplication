package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

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
    /*ArrayList<String> chatList;
    ArrayAdapter<String> adapter;*/
    ChatAdapter chatAdapter;
    String chatId, senderId, senderName;
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

        loading.setVisibility(View.VISIBLE);
        send.setEnabled(false);

        /*chatList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatList); //adapter for chatList
        chat.setAdapter(adapter);*/
        chatAdapter = new ChatAdapter(getApplicationContext(), R.id.sendMsg);
        chat.setAdapter(chatAdapter);
        chat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        chat.setAdapter(chatAdapter);
        chatAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                chat.setSelection(chatAdapter.getCount() - 1);
            }
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        //details of another user in chat
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("Preference", MODE_PRIVATE);
        String chatUserName = sharedPref.getString("chatUser", "User");
        String chatUserId = sharedPref.getString("chatUserId", null);
        /*Log.e("ChatId", "another user Id and name > " + chatUserId + " " + chatUserId);*/

        //name of another chat user
        chatUserNameTV.setText(chatUserName);

        //details of the current user
        senderName = Objects.requireNonNull(auth.getCurrentUser()).getDisplayName();
        senderId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        /*Log.e("CurrentUser", "Current user name and id: "+ senderName +" "+ senderId);*/

        //possible chatId's for storing chat data
        String chatId1 = senderId + chatUserId;
        String chatId2 = chatUserId + senderId;

        //is there any chatId already existed from these possibilities, if not then chatId2 will be set as chatId
        chatId = checkChatId(chatId1, chatId2);
        /*Log.e("chatId", "current Chat Id: "+ chatId);*/

        //snapshot listener, when ever there is changes in the chat
        chatListener(chatId);

        //swipe down to refresh the chatList
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e("check_Id", "chatId is : "+ chatId);
                getChatData(chatId);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        /*Log.e("time", "get time: "+ timeStamp);*/

        // send the message
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMsg = msg.getText().toString().trim();
                if(!newMsg.isEmpty()){
                    msgModel model = new msgModel(senderName, newMsg, timeStamp);
                    documentReference = db.collection("ChatData").document(chatId);
                    //store to the fireStore db, document already exists then update the array if not then create new document and set new data
                    checkDocumentAndAdd(model, documentReference);
                }
            }
        });

        chatUserNameTV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("userName", chatUserId);
                clipboard.setPrimaryClip(clipData);
                return false;
            }
        });
    }

    private void chatListener(String chatId) {
        //reference for the chatId document section
        documentReference = db.collection("ChatData").document(chatId);
        //changes happens in document. addSnapshotListener, starts listening to the document referenced by above DocumentReference.
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                assert value != null;
                if(value.exists()){
                    getChatData(chatId);
                }
            }
        });
    }

    private void checkDocumentAndAdd(msgModel model, DocumentReference documentReference) {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        //if document does exists then update the firebase message array
                        addMsg(model, documentReference);
                    } else {
                        //if document doesn't exists then set the firebase message array
                        onFirstMsg(model, documentReference);
                    }
                    getChatData(chatId);
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

    private void addMsg(msgModel model, DocumentReference documentReference) {
        documentReference.update("Message", FieldValue.arrayUnion(model)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                msg.setText("");
                Toast.makeText(ChatScreen.this, "Data added to db", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatScreen.this, "First msg", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //checks is there any chatId already existed or not if not then chatId2 will be the chatId
    private String checkChatId(String chatId1, String chatId2){
        loading.setVisibility(View.VISIBLE);
        DocumentReference documentReference = db.collection("ChatData").document(chatId1);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    chatId = chatId1;
                } else {
                    chatId = chatId2;
                }
                chatListener(chatId);
                getChatData(chatId);
                send.setEnabled(true);
                /*Log.e("check_chatId","current chat Id: " + chatId);*/
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
            /*Log.e("check_chatId","current chat Id in if: " + chatId);*/
        }
        return chatId;
    }

    //starts getting the entire chat data with chatId
    private void getChatData(String chatId) {
        DocumentReference documentReference = db.collection("ChatData").document(chatId);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                loading.setVisibility(View.GONE);
                if(document.exists()){
                    loading.setVisibility(View.VISIBLE);

                    ArrayList<HashMap<String, String>> users = (ArrayList<HashMap<String, String>>) document.get("Message");
                    assert users != null;
                    /*Log.e("ChatData", "Chat > "+ users.size());*/
                    loading.setVisibility(View.GONE);
                    chatAdapter.clear();

                    for(int i = 0 ; i<users.size(); i++){
                        HashMap<String, String> chat = users.get(i);
                        String msg = chat.get("msg");
                        String userName = chat.get("userId");
                        String chatMsg = userName + ":\n" + msg +"\n\n";
                        if(Objects.equals(userName, senderName)){
                            chatAdapter.add(new ChatMesgDirection(true, chatMsg));
                        } else {
                            chatAdapter.add(new ChatMesgDirection(false, chatMsg));
                        }
                    }
                }
            }
        });
    }
}