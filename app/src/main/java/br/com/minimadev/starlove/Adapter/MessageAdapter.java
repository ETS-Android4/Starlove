package br.com.minimadev.starlove.Adapter;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import br.com.minimadev.starlove.Objects.MessageObject;
import br.com.minimadev.starlove.R;


import java.util.List;

/**
 * Adapter responsible for handling the display of the messages to the user
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolders>{
    private List<MessageObject> chatList;
    private Context context;


    public MessageAdapter(List<MessageObject> matchesList, Context context){
        this.chatList = matchesList;
        this.context = context;
    }

    @Override
    public MessageViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        MessageViewHolders rcv = new MessageViewHolders(layoutView);

        return rcv;
    }


    /**
     * Populate the item_message with user in the current position
     *
     * Changes the message aspect if it is from the current user or the match
     *
     * @param holder
     * @param position - position of the list
     */
    @Override
    public void onBindViewHolder(MessageViewHolders holder, int position) {

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.mMessage.getLayoutParams();
        params.leftMargin = 20; params.rightMargin = 20;
        holder.mMessage.setLayoutParams(params);

        holder.mMessage.setText(chatList.get(position).getMessage());

        if(chatList.get(position).getCurrentUser()){
            holder.mMessage.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
            holder.mLayout.setGravity(Gravity.END);
            holder.mContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.message_right));
        }else{
            holder.mMessage.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
            holder.mLayout.setGravity(Gravity.START);
            holder.mContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.message_left));
        }
    }


    @Override
    public int getItemCount() {
        return this.chatList.size();
    }


    class MessageViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mMessage;
        public LinearLayout mContainer, mLayout;
        public MessageViewHolders(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mLayout = itemView.findViewById(R.id.layout);
            mMessage = itemView.findViewById(R.id.message);
            mContainer = itemView.findViewById(R.id.container);
        }

        @Override
        public void onClick(View view) {
        }
    }

}
