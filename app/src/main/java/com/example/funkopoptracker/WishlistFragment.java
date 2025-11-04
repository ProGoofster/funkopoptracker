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

public class WishlistFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        ListView listView = view.findViewById(R.id.wishlistListView);

        List<FunkoPop> wishlistPops = new ArrayList<>();

        //fake funko pops to populate wishlist
        wishlistPops.add(new FunkoPop("Darth Vader", 1));
        wishlistPops.add(new FunkoPop("Groot", 49));
        wishlistPops.add(new FunkoPop("Deadpool", 111));

        ArrayAdapter<FunkoPop> adapter = new ArrayAdapter<FunkoPop>(
            getContext(),
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            wishlistPops
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
