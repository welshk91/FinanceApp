package com.databases.example.data;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.databases.example.R;
import com.fima.cardsui.objects.Card;

public class MyPlayCard extends Card {

    public MyPlayCard(String titlePlay, String description, String color,
                      String titleColor, Boolean hasOverflow, Boolean isClickable) {
        super(titlePlay, description, color, titleColor, hasOverflow,
                isClickable);
    }

    @Override
    public View getCardContent(Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_play, null);

        ((TextView) v.findViewById(R.id.title)).setText(titlePlay);
        ((TextView) v.findViewById(R.id.title)).setTextColor(Color
                .parseColor(titleColor));
        ((TextView) v.findViewById(R.id.description)).setText(description);
        v.findViewById(R.id.stripe).setBackgroundColor(Color
                .parseColor(color));

        if (isClickable)
            v.findViewById(R.id.content_layout)
                    .setBackgroundResource(R.drawable.selectable_background_cardbank);

        if (hasOverflow)
            v.findViewById(R.id.overflow)
                    .setVisibility(View.VISIBLE);
        else
            v.findViewById(R.id.overflow)
                    .setVisibility(View.GONE);

        return v;
    }

}
