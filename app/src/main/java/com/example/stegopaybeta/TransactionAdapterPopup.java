package com.example.stegopaybeta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

        SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputDate = new SimpleDateFormat("dd/MM/yyyy");

        try {
            Date currentDate = inputDate.parse(currentTransaction.getDate().split("T")[0]);
            date.setText(outputDate.format(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        TextView store = (TextView) listItem.findViewById(R.id.tv_store_single_popup);
        store.setText(currentTransaction.getVendor());

        TextView amount = (TextView) listItem.findViewById(R.id.tv_amount_single_popup);
        String amount_with_currency = currentTransaction.getCurrency() + " " + String.format("%.2f", Double.parseDouble(currentTransaction.getAmount()));
        amount.setText(amount_with_currency);

        return listItem;

    }

}
