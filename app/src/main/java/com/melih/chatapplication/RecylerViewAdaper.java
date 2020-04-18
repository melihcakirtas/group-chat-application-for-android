package com.melih.chatapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParser;

import java.util.List;

import static com.melih.chatapplication.R.layout.recyler_list_row;

public class RecylerViewAdaper extends RecyclerView.Adapter<RecylerViewAdaper.MyViewHolder> {

    private List<String> chatMessages;
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(recyler_list_row,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String chatMessage = chatMessages.get(position);
        holder.chatMessage.setText(chatMessage);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public RecylerViewAdaper(List<String> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView chatMessage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            chatMessage = itemView.findViewById(R.id.recyclerView_text);
        }
    }

}
