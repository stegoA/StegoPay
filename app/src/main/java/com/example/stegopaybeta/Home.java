package com.example.stegopaybeta;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Home extends AppCompatActivity {


    ListView activityListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_details);

//        activityListView = (ListView) findViewById(R.id.activityListView);
//
//        Activity ac1 = new Activity("Netflix", 12000);
//        Activity ac2 = new Activity("Adidas", 1000);
//
//        ArrayList<Activity> myActivity = new ArrayList<>();
//
//        myActivity.add(ac1);
//        myActivity.add(ac2);
//
//        ActivityListAdapter adapter = new ActivityListAdapter(this, R.layout.adapter_view_layout, myActivity);
//        activityListView.setAdapter(adapter);

    }


}
