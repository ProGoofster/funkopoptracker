package com.example.funkopoptracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ListView listView = view.findViewById(R.id.funkoPopsListView);

        List<FunkoPop> funkoPops = new ArrayList<>();

        //fake funko pops to populate listview on home page
        funkoPops.add(new FunkoPop("Baby Yoda", "#368"));
        funkoPops.add(new FunkoPop("Iron Man", "#285"));
        funkoPops.add(new FunkoPop("Spider-Man", "#593"));

        ArrayAdapter<FunkoPop> adapter = new ArrayAdapter<FunkoPop>(
            getContext(),
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            funkoPops
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                //simple_list_item_2 has text1 for title and text2 for subtitle
                android.widget.TextView popNameText = view.findViewById(android.R.id.text1);
                android.widget.TextView popNumberText = view.findViewById(android.R.id.text2);
                FunkoPop funkoPop = getItem(position);
                popNameText.setText(funkoPop.getName());
                popNumberText.setText(funkoPop.getNumber());
                return view;
            }
        };

        listView.setAdapter(adapter);

        return view;
    }
}
