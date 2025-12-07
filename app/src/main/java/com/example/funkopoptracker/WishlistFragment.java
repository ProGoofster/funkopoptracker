package com.example.funkopoptracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.funkopoptracker.database.FunkoContentProvider;

import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {

    private List<FunkoPop> allFunkoPops = new ArrayList<>();
    private List<FunkoPop> filteredFunkoPops = new ArrayList<>();
    private ArrayAdapter<FunkoPop> adapter;
    private TextView emptyText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        ListView listView = view.findViewById(R.id.wishlistListView);
        SearchView searchView = view.findViewById(R.id.searchView);
        emptyText = view.findViewById(R.id.wishlistEmptyText);

        // Clear lists to prevent duplication when returning from back stack
        allFunkoPops.clear();
        filteredFunkoPops.clear();

        Cursor cursor = getActivity().getContentResolver().query(FunkoContentProvider.CONTENT_URI_WISHLIST, null, null, null, null);

        allFunkoPops.addAll(FunkoPop.allFromCursor(cursor));
        filteredFunkoPops.addAll(allFunkoPops);

        updateEmptyTextVisibility();

        adapter = new ArrayAdapter<FunkoPop>(
            getContext(),
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            filteredFunkoPops
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                //simple_list_item_2 has text1 for title and text2 for subtitle
                android.widget.TextView popNameText = view.findViewById(android.R.id.text1);
                android.widget.TextView popNumberText = view.findViewById(android.R.id.text2);
                FunkoPop funkoPop = getItem(position);
                popNameText.setText(funkoPop.getName());
                popNumberText.setText(funkoPop.getNumberString());
                return view;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            FunkoPop clickedPop = filteredFunkoPops.get(position);

            WishlistViewPopFragment fragment = WishlistViewPopFragment.newInstance(clickedPop);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFunkoPops(newText);
                return true;
            }
        });

        return view;
    }

    private void filterFunkoPops(String query) {
        filteredFunkoPops.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredFunkoPops.addAll(allFunkoPops);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (FunkoPop funkoPop : allFunkoPops) {
                // Search by name or number
                if (funkoPop.getName().toLowerCase().contains(lowerCaseQuery) ||
                    funkoPop.getNumberString().toLowerCase().contains(lowerCaseQuery)) {
                    filteredFunkoPops.add(funkoPop);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyTextVisibility();
    }

    private void updateEmptyTextVisibility() {
        if (filteredFunkoPops.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }
}
