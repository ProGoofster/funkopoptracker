package com.example.funkopoptracker;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.funkopoptracker.database.FunkoContentProvider;

import java.util.ArrayList;
import java.util.List;

public class ValueFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_value, container, false);

        TextView totalValueText = view.findViewById(R.id.totalValueText);
        ListView listView = view.findViewById(R.id.valueListView);

        List<FunkoPop> funkoPops = new ArrayList<>();

        Cursor cursor = getActivity().getContentResolver().query(FunkoContentProvider.CONTENT_URI, null, null, null, null);
        funkoPops.addAll(FunkoPop.allFromCursor(cursor));

        int totalValue = funkoPops.size() * 50;//debug - all values set to 50 rn
        totalValueText.setText("Total Value: $" + totalValue);



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
                popNumberText.setText(funkoPop.getNumber() + " - $50");
                return view;
            }
        };

        listView.setAdapter(adapter);

        return view;
    }
}
