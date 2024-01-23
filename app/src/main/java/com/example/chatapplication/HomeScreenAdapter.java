package com.example.chatapplication;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
