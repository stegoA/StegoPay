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

public class TransactionAdapterPopup extends ArrayAdapter<Transaction> {
    private Context mContext;
    private List<Transaction> transactionsList = new ArrayList<>();

    public TransactionAdapterPopup(@NonNull Context context, List<Transaction> list) {
        super(context, 0,list);
        mContext = context;
        transactionsList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null){
            listItem = LayoutInflater.from(mContext).inflate(R.layout.popup_single_transaction_list,parent,false);
        }

        Transaction currentTransaction = transactionsList.get(position);

        TextView date = (TextView) listItem.findViewById(R.id.tv_date_single_popup);
        //Set the date of the transaction
        date.setText(currentTransaction.getDate().split("T")[0]);

        TextView store = (TextView) listItem.findViewById(R.id.tv_store_single_popup);
        store.setText(currentTransaction.getVendor());

        TextView amount = (TextView) listItem.findViewById(R.id.tv_amount_single_popup);
        String amount_with_currency = currentTransaction.getCurrency() +" "+currentTransaction.getAmount();
        amount.setText(amount_with_currency);

        return listItem;

    }

}
