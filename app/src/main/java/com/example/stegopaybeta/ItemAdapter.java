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

public class ItemAdapter extends ArrayAdapter<Item> {

    private Context mContext;
    private List<Item> itemList = new ArrayList<>();

    public ItemAdapter(@NonNull Context context, ArrayList<Item> list) {
        super(context,0, list);
        mContext = context;
        itemList = list;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.single_order_item, parent, false);
        }

        Item currentItem = itemList.get(position);

        TextView orderItem = (TextView) listItem.findViewById(R.id.orderItemTextView);
        orderItem.setText(currentItem.getName());

        TextView quantity = (TextView) listItem.findViewById(R.id.quantityTextView);
        quantity.setText("x " + currentItem.getQuantity());

        TextView amount = (TextView) listItem.findViewById(R.id.tv_amount);
        String amountFormatted =  " - "+ String.format("%.2f", Double.parseDouble(currentItem.getProduct_price()));
        amount.setText(amountFormatted);

        return listItem;

    }


}
