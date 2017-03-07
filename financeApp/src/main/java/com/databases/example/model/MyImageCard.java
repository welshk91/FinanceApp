package com.databases.example.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.databases.example.R;
import com.fima.cardsui.objects.Card;

public class MyImageCard extends Card{
    public MyImageCard(String title, int image){
        super(title, image);
    }

    @Override
    public View getCardContent(Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_picture, null);

        ((TextView) v.findViewById(R.id.title)).setText(title);
        ((ImageView) v.findViewById(R.id.image_view)).setImageResource(image);

        return v;
    }

}
