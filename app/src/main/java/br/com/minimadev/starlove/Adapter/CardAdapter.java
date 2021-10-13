package br.com.minimadev.starlove.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import br.com.minimadev.starlove.Objects.UserObject;
import br.com.minimadev.starlove.R;


import java.util.List;


/**
 * Adapter responsible for handling the display of the Cards to the user
 */
public class CardAdapter extends ArrayAdapter<UserObject>{

    Context context;

    public CardAdapter(Context context, int resourceId, List<UserObject> items){
        super(context, resourceId, items);
    }

    /**
     * Populate the item_message with user in the current position
     *
     * Changes the message aspect if it is from the current user or the match
     *
     * @param position - position of the list
     */
    public View getView(int position, View convertView, ViewGroup parent){
        UserObject card_item = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_card, parent, false);
        }

        TextView name = convertView.findViewById(R.id.name);
        ImageView image = convertView.findViewById(R.id.image);

        name.setText(card_item.getName() + ", Signo:" + card_item.getSigno());

        if(!card_item.getProfileImageUrl().equals("default"))
            Glide.with(convertView.getContext()).load(card_item.getProfileImageUrl()).into(image);



        return convertView;

    }
}
