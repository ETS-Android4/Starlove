package br.com.minimadev.starlove.Adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import br.com.minimadev.starlove.Activity.ChatActivity;
import br.com.minimadev.starlove.Objects.MatchesObject;
import br.com.minimadev.starlove.R;


import java.util.List;


/**
 * Adapter responsible for handling the display of the chats available to the user
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolders>{
    private List<MatchesObject> matchesList;
    private Context context;


    public ChatListAdapter(List<MatchesObject> matchesList, Context context){
        this.matchesList = matchesList;
        this.context = context;
    }

    @Override
    public ChatListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        ChatListViewHolders rcv = new ChatListViewHolders(layoutView);

        return rcv;
    }

    /**
     * Populate the item_chat_list with user in the current position
     * @param holder
     * @param position - position of the list
     */
    @Override
    public void onBindViewHolder(ChatListViewHolders holder, int position) {

        holder.mLayout.setTag(matchesList.get(position).getUser().getUserId());
        holder.mName.setText(matchesList.get(position).getUser().getName());
        holder.mMessage.setText(matchesList.get(position).getLastMessage());
        if(!matchesList.get(position).getUser().getProfileImageUrl().equals("default"))
            Glide.with(context).load(matchesList.get(position).getUser().getProfileImageUrl()).apply(RequestOptions.circleCropTransform()).into(holder.mImage);

    }

    @Override
    public int getItemCount() {
        return this.matchesList.size();
    }

    class ChatListViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mName,
                mMessage;
        public ImageView mImage;
        public LinearLayout mLayout;
        public ChatListViewHolders(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mLayout = itemView.findViewById(R.id.layout);
            mName = itemView.findViewById(R.id.name);
            mMessage = itemView.findViewById(R.id.message);
            mImage = itemView.findViewById(R.id.image);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), ChatActivity.class);
            Bundle b = new Bundle();
            b.putString("matchId", mLayout.getTag().toString());
            intent.putExtras(b);
            view.getContext().startActivity(intent);
        }
    }

}
