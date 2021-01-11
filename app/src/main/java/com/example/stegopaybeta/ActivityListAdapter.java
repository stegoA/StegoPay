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

public class ActivityListAdapter extends ArrayAdapter<Activity> {

    private Context mContext;
    int mResource;

    public ActivityListAdapter(Context context, int resource, ArrayList<Activity> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String service = getItem(position).getService();
        double amount = getItem(position).getAmount();

        Activity activity = new Activity(service, amount);

        LayoutInflater inflater = LayoutInflater.from(mContext);

        convertView = inflater.inflate(mResource, parent, false);

        TextView tvService = (TextView) convertView.findViewById(R.id.serviceTextView);
        TextView tvAmount = (TextView) convertView.findViewById(R.id.amountTextView);

        tvService.setText(service);
        tvAmount.setText(amount + "");

        return convertView;


    }
}
