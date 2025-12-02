package com.example.funkopoptracker;

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

public class HomeFragment extends Fragment {

    private List<FunkoPop> allFunkoPops = new ArrayList<>();
    private List<FunkoPop> filteredFunkoPops = new ArrayList<>();
    private ArrayAdapter<FunkoPop> adapter;
    private TextView emptyText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ListView listView = view.findViewById(R.id.funkoPopsListView);
        SearchView searchView = view.findViewById(R.id.searchView);
        emptyText = view.findViewById(R.id.collectionEmptyText);

        // Clear lists to prevent duplication when returning from back stack
        allFunkoPops.clear();
        filteredFunkoPops.clear();

        Cursor cursor = getActivity().getContentResolver().query(FunkoContentProvider.CONTENT_URI_OWNED, null, null, null, null);

        allFunkoPops.addAll(FunkoPop.allFromCursor(cursor));
        filteredFunkoPops.addAll(allFunkoPops);

        updateEmptyTextVisibility();

        adapter = new ArrayAdapter<FunkoPop>(
            getContext(),
            R.layout.list_item_funko_pop,
            filteredFunkoPops
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_funko_pop, parent, false);
                }

                android.widget.TextView popNameText = convertView.findViewById(R.id.txtFunkoPopName);
                android.widget.TextView popNumberText = convertView.findViewById(R.id.txtFunkoPopNumber);
                android.widget.ImageView imgThumbnail = convertView.findViewById(R.id.imgFunkoPopThumbnail);

                FunkoPop funkoPop = getItem(position);
                popNameText.setText(funkoPop.getName());
                popNumberText.setText(funkoPop.getNumberString());

                // Load image if available
                String picturePath = funkoPop.getPicture();
                if (picturePath != null && !picturePath.isEmpty()) {
                    try {
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(picturePath);
                        if (bitmap != null) {
                            imgThumbnail.setImageBitmap(bitmap);
                            imgThumbnail.setVisibility(View.VISIBLE);
                        } else {
                            imgThumbnail.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        imgThumbnail.setVisibility(View.GONE);
                    }
                } else {
                    imgThumbnail.setVisibility(View.GONE);
                }

                return convertView;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            FunkoPop selectedPop = filteredFunkoPops.get(position);
            ViewPopFragment viewPopFragment = ViewPopFragment.newInstance(selectedPop);

            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, viewPopFragment)
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
