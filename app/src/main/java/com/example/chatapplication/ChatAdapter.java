package com.example.chatapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends ArrayAdapter<ChatMesgDirection> {

    private List<ChatMesgDirection> chatList = new ArrayList<>();
    public ChatAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }
    @Override
    public void add(ChatMesgDirection object){
        chatList.add(object);
        super.add(object);
    }
    @Override
    public int getCount(){
        return this.chatList.size();
    }
    public void clear(){
        chatList.clear();
    }
    public ChatMesgDirection getItem(int index){
        return this.chatList.get(index);
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        View row = convertView;
        ChatMesgDirection chatMesgDirection = getItem(position);

        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert chatMesgDirection != null;
        if(chatMesgDirection.left){
            row = inflater.inflate(R.layout.msg_send, parent, false);
        }else {
            row = inflater.inflate(R.layout.msg_receive, parent, false);
        }
        TextView chatTxt = row.findViewById(R.id.chatTxt);
        chatTxt.setText(chatMesgDirection.message);
        return row;
    }

}
