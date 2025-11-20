package com.example.funkopoptracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class ViewPopFragment extends Fragment {
    private static final String ARG_NAME = "name";
    private static final String ARG_NUMBER = "number";
    private static final String ARG_RARITY = "rarity";
    private static final String ARG_PICTURE = "picture";
    private static final String ARG_PRICE = "price";

    private String name;
    private int number;
    private int rarity;
    private String picture;
    private double price;

    public static ViewPopFragment newInstance(FunkoPop funkoPop) {
        ViewPopFragment fragment = new ViewPopFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, funkoPop.getName());
        args.putInt(ARG_NUMBER, funkoPop.getNumber());
        args.putInt(ARG_RARITY, funkoPop.getRarity());
        args.putString(ARG_PICTURE, funkoPop.getPicture());
        args.putDouble(ARG_PRICE, funkoPop.getPrice());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_NAME);
            number = getArguments().getInt(ARG_NUMBER);
            rarity = getArguments().getInt(ARG_RARITY);
            picture = getArguments().getString(ARG_PICTURE);
            price = getArguments().getDouble(ARG_PRICE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_pop, container, false);

        // Setup back button
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Populate the views with data
        TextView tvFunkoName = view.findViewById(R.id.tvFunkoName);
        TextView tvFunkoNumber = view.findViewById(R.id.tvFunkoNumber);
        RatingBar ratingBarRarity = view.findViewById(R.id.ratingBarRarity);
        TextView tvRarityValue = view.findViewById(R.id.tvRarityValue);
        TextView tvCurrentValue = view.findViewById(R.id.tvCurrentValue);
        ImageView imgFunkoPop = view.findViewById(R.id.imgFunkoPop);

        tvFunkoName.setText(name);
        tvFunkoNumber.setText("#" + number);
        ratingBarRarity.setRating(rarity);
        tvRarityValue.setText("(" + rarity + ".0)");
        tvCurrentValue.setText("$" + price);

        // TODO: Load image from picture URL if available
        // For now, you can implement image loading later
        if (picture != null && !picture.isEmpty()) {
            // Load image using Glide, Picasso, or similar library
        }

        return view;
    }
}