package com.example.funkopoptracker;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.funkopoptracker.database.FunkoContentProvider;

import java.io.File;

public class WishlistViewPopFragment extends Fragment {
    private static final String ARG_NAME = "name";
    private static final String ARG_NUMBER = "number";
    private static final String ARG_RARITY = "rarity";
    private static final String ARG_PICTURE = "picture";
    private static final String ARG_PRICE = "price";

    private int id;
    private String name;
    private int number;
    private int rarity;
    private String picture;
    private double price;

    public static WishlistViewPopFragment newInstance(FunkoPop funkoPop) {
        WishlistViewPopFragment fragment = new WishlistViewPopFragment();
        Bundle args = new Bundle();
        args.putInt("id", funkoPop.getId());
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
            id = getArguments().getInt("id");
            name = getArguments().getString(ARG_NAME);
            number = getArguments().getInt(ARG_NUMBER);
            rarity = getArguments().getInt(ARG_RARITY);
            picture = getArguments().getString(ARG_PICTURE);
            price = getArguments().getDouble(ARG_PRICE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist_view_pop, container, false);

        // Setup back button
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        //Setup delete button
        Button btnDelete = view.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> {
            // Delete the image file if it exists
            if (picture != null && !picture.isEmpty()) {
                File imageFile = new File(picture);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }

            // Delete the entry from the database using name and number as selection criteria
            String selection = FunkoContentProvider.COL_NAME + " = ? AND " + FunkoContentProvider.COL_NUMBER + " = ?";
            String[] selectionArgs = new String[]{name, String.valueOf(number)};

            int deletedRows = getActivity().getContentResolver().delete(
                FunkoContentProvider.CONTENT_URI_WISHLIST,
                selection,
                selectionArgs
            );

            if (deletedRows > 0) {
                android.widget.Toast.makeText(getContext(), "Funko Pop deleted", android.widget.Toast.LENGTH_SHORT).show();
                // Navigate back after deletion
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                android.widget.Toast.makeText(getContext(), "Failed to delete", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        Button btnWishlist = view.findViewById(R.id.btnAddToWishlist);
        btnWishlist.setOnClickListener(v -> {
            //check if already in collection
            Cursor cursor = getActivity().getContentResolver().query(FunkoContentProvider.CONTENT_URI_OWNED,
                null, "NAME = ? AND NUMBER = ?", new String[]{name, String.valueOf(number)}, null);
            if (cursor.getCount() > 0) {
                cursor.close();
                android.widget.Toast.makeText(getContext(), "Already in collection", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            cursor.close();

            //remove from wishlist
            getActivity().getContentResolver().delete(FunkoContentProvider.CONTENT_URI_WISHLIST,
                "NAME = ? AND NUMBER = ?", new String[]{name, String.valueOf(number)});

            //add to collection
            FunkoPop pop = new FunkoPop(name, number, rarity, picture, price);
            getActivity().getContentResolver().insert(FunkoContentProvider.CONTENT_URI_OWNED, pop.toContentValues());

            android.widget.Toast.makeText(getContext(), "Added to collection", android.widget.Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Populate the views with data
        TextView tvFunkoName = view.findViewById(R.id.tvFunkoName);
        TextView tvFunkoNumber = view.findViewById(R.id.tvFunkoNumber);
        RatingBar ratingBarRarity = view.findViewById(R.id.ratingBarRarity);
        TextView tvRarityValue = view.findViewById(R.id.tvRarityValue);
        TextView tvCurrentValue = view.findViewById(R.id.tvCurrentValue);
        ImageView imgFunkoPop = view.findViewById(R.id.imgFunkoPop);
        View cardViewImage = view.findViewById(R.id.cardViewImage);

        tvFunkoName.setText(name);
        tvFunkoNumber.setText("#" + number);
        ratingBarRarity.setRating(rarity);
        tvRarityValue.setText("(" + rarity + ".0)");
        tvCurrentValue.setText("$" + String.format("%.2f", price));

        // Load image from internal storage if available
        if (picture != null && !picture.isEmpty()) {
            try {
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(picture);
                if (bitmap != null) {
                    imgFunkoPop.setImageBitmap(bitmap);
                    cardViewImage.setVisibility(View.VISIBLE); // Show the card view when image is available
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return view;
    }
}