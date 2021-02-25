package com.example.stegopaybeta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends ArrayAdapter<Card> {

    private Context mContext;
    private List<Card> cardList = new ArrayList<>();

    public CardAdapter(@NonNull Context context, ArrayList<Card> list) {
        super(context, 0, list);
        mContext = context;
        cardList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.single_card, parent, false);
        }

        Card currentCard = cardList.get(position);

        TextView cardNickname = (TextView) listItem.findViewById(R.id.tv_cardNickname);
        cardNickname.setText(currentCard.getNickName());

        TextView last4Digits = (TextView) listItem.findViewById(R.id.tv_last4Digits);
        last4Digits.setText("****" + currentCard.getLast4Digits());

        return listItem;

    }
}
