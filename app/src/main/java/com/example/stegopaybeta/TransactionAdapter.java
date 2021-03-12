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

public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private Context mContext;
    private List<Transaction> transactionList = new ArrayList<>();

    public TransactionAdapter(@NonNull Context context, ArrayList<Transaction> list) {
        super(context, 0, list);
        mContext = context;
        transactionList = list;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
       View listItem = convertView;
       if(listItem == null) {
           listItem = LayoutInflater.from(mContext).inflate(R.layout.single_transaction, parent, false);
       }

       Transaction currentTransaction = transactionList.get(position);

       TextView vendor = (TextView) listItem.findViewById(R.id.tv_vendor);
       vendor.setText(currentTransaction.getVendor());

       TextView amount = (TextView) listItem.findViewById(R.id.tv_amount);
       String amount_with_currency = currentTransaction.getCurrency() + " - "+currentTransaction.getAmount();
       amount.setText(amount_with_currency);

       return listItem;

    }
}
