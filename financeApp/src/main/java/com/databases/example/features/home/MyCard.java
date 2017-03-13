package com.databases.example.features.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.databases.example.R;
import com.fima.cardsui.objects.Card;

public class MyCard extends Card {
    public MyCard(String title, String desc){
        super(title, desc);
    }

    @Override
    public View getCardContent(Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_ex, null);
        ((TextView) v.findViewById(R.id.title)).setText(title);
        ((TextView) v.findViewById(R.id.description)).setText(desc);
        return v;
    }
}
