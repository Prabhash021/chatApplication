package com.example.chatapplication;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.InputStream;
import java.util.ArrayList;

public class HomeScreenAdapter extends RecyclerView.Adapter<HomeScreenAdapter.ViewHolder> {
    Context context;
    ArrayList<UserDataModel> arrayList;

    HomeScreenAdapter(@NonNull Context context,@NonNull ArrayList<UserDataModel> arrayList){
        this.context = context;
        this.arrayList = arrayList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.userlist, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Uri uri = arrayList.get(position).getProfileUri();

        Log.e("Adapter", "Uri link >" + uri);

        if(uri != null){
            Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.profileIV);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_launcher_foreground)
                    .into(holder.profileIV);
        }

        holder.userProfileName.setText(arrayList.get(position).getName());
        holder.userEmail.setText(arrayList.get(position).getEmail());

        String user = arrayList.get(position).getName();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("Preference", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("chatUser", user);
                editor.apply();
                Intent intent = new Intent(v.getContext(), ChatScreen.class);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profileIV;
        TextView userProfileName, userEmail;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileIV = itemView.findViewById(R.id.userProfileImgPreview);
            userProfileName = itemView.findViewById(R.id.UserNameTV);
            userEmail = itemView.findViewById(R.id.UserEmailTV);

        }
    }
}
