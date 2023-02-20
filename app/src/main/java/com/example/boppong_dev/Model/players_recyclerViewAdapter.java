package com.example.boppong_dev.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.ResourceManagerInternal;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boppong_dev.R;

import java.util.ArrayList;

public class players_recyclerViewAdapter extends RecyclerView.Adapter<players_recyclerViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<Player>players;

    public players_recyclerViewAdapter(Context context, ArrayList<Player>players){
        this.context = context;
        this.players = players;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_row, parent, false);
        return new players_recyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull players_recyclerViewAdapter.MyViewHolder holder, int position) {
        holder.playerName.setText(players.get(position).name);
        if (players.get(position).songSubmission.getId()!=null){
            holder.playerStatus.setText("Subbed");
        }
        else{
            holder.playerStatus.setText("unSubbed");
        }
        holder.playerPicture.setImageResource(players.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView playerName,playerStatus;
        ImageView playerPicture;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            playerPicture = itemView.findViewById(R.id.playerImageView);
            playerName = itemView.findViewById(R.id.playerNameView);
            playerStatus = itemView.findViewById(R.id.playerStatusView);
        }
    }
}
