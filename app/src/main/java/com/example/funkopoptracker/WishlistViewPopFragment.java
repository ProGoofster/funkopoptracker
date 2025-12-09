package com.example.funkopoptracker;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.content.ContentValues;

import com.example.funkopoptracker.database.FunkoContentProvider;

import java.io.File;

public class WishlistViewPopFragment extends Fragment {
    private static final String ARG_NAME = "name";
    private static final String ARG_NUMBER = "number";
    private static final String ARG_RARITY = "rarity";
    private static final String ARG_PICTURE = "picture";
    private static final String ARG_PRICE = "price";
    private static final String ARG_CONDITION = "condition";
    private static final String ARG_NOTES = "notes";
    private static final String ARG_DATE_ADDED = "dateAdded";
    private static final String ARG_ORIGINAL_PRICE = "originalPrice";

    private int id;
    private String name;
    private int number;
    private int rarity;
    private String picture;
    private double price;
    private String condition;
    private String notes;
    private long dateAdded;
    private double originalPrice;

    // UI elements - View mode
    private TextView tvFunkoName;
    private TextView tvFunkoNumber;
    private TextView tvCondition;
    private TextView tvNotes;

    // UI elements - Edit mode
    private EditText editFunkoName;
    private EditText editFunkoNumber;
    private Spinner spinnerCondition;
    private EditText editNotes;

    private Button btnEdit;
    private boolean isEditMode = false;

    public static WishlistViewPopFragment newInstance(FunkoPop funkoPop) {
        WishlistViewPopFragment fragment = new WishlistViewPopFragment();
        Bundle args = new Bundle();
        args.putInt("id", funkoPop.getId());
        args.putString(ARG_NAME, funkoPop.getName());
        args.putInt(ARG_NUMBER, funkoPop.getNumber());
        args.putInt(ARG_RARITY, funkoPop.getRarity());
        args.putString(ARG_PICTURE, funkoPop.getPicture());
        args.putDouble(ARG_PRICE, funkoPop.getPrice());
        args.putString(ARG_CONDITION, funkoPop.getCondition());
        args.putString(ARG_NOTES, funkoPop.getNotes());
        args.putLong(ARG_DATE_ADDED, funkoPop.getDateAdded());
        args.putDouble(ARG_ORIGINAL_PRICE, funkoPop.getOriginalPrice());
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
            condition = getArguments().getString(ARG_CONDITION, "Mint");
            notes = getArguments().getString(ARG_NOTES, "");
            dateAdded = getArguments().getLong(ARG_DATE_ADDED, 0);
            originalPrice = getArguments().getDouble(ARG_ORIGINAL_PRICE, price);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist_view_pop, container, false);

        //Initialize all UI elements
        tvFunkoName = view.findViewById(R.id.tvFunkoName);
        tvFunkoNumber = view.findViewById(R.id.tvFunkoNumber);
        tvCondition = view.findViewById(R.id.tvCondition);
        tvNotes = view.findViewById(R.id.tvNotes);

        editFunkoName = view.findViewById(R.id.editFunkoName);
        editFunkoNumber = view.findViewById(R.id.editFunkoNumber);
        spinnerCondition = view.findViewById(R.id.spinnerCondition);
        editNotes = view.findViewById(R.id.editNotes);

        btnEdit = view.findViewById(R.id.btnEdit);

        //Setup condition spinner
        String[] conditions = {"Mint", "Near Mint", "Good", "Fair", "Poor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, conditions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(adapter);

        //Setup back button
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

        //Setup edit/save button
        btnEdit.setOnClickListener(v -> {
            if (isEditMode) {
                saveChanges();
            } else {
                enterEditMode();
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
            FunkoPop pop = new FunkoPop(name, number, rarity, picture, price, condition, notes);
            android.widget.Toast.makeText(getContext(), "Added to collection", android.widget.Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Populate the views with data
        RatingBar ratingBarRarity = view.findViewById(R.id.ratingBarRarity);
        TextView tvRarityValue = view.findViewById(R.id.tvRarityValue);
        TextView tvCurrentValue = view.findViewById(R.id.tvCurrentValue);
        ImageView imgFunkoPop = view.findViewById(R.id.imgFunkoPop);
        View cardViewImage = view.findViewById(R.id.cardViewImage);

        tvFunkoName.setText(name);
        tvFunkoNumber.setText("#" + number);
        tvCondition.setText(condition);
        tvNotes.setText(notes.isEmpty() ? "No notes available" : notes);
        ratingBarRarity.setRating(rarity);
        tvRarityValue.setText("(" + rarity + ".0)");
        tvCurrentValue.setText("$" + String.format("%.2f", price));

        //date added and original price
        TextView tvDateAdded = view.findViewById(R.id.tvDateAdded);
        TextView tvOriginalPrice = view.findViewById(R.id.tvOriginalPrice);
        if (dateAdded > 0) {
            tvDateAdded.setText(android.text.format.DateUtils.formatDateTime(getContext(), dateAdded, android.text.format.DateUtils.FORMAT_SHOW_DATE));
        } else {
            tvDateAdded.setText("N/A");
        }
        tvOriginalPrice.setText("$" + String.format("%.2f", originalPrice));

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
    private void enterEditMode() {
        isEditMode = true;
        btnEdit.setText("Save");

        tvFunkoName.setVisibility(View.GONE);
        editFunkoName.setVisibility(View.VISIBLE);
        editFunkoName.setText(name);

        tvFunkoNumber.setVisibility(View.GONE);
        editFunkoNumber.setVisibility(View.VISIBLE);
        editFunkoNumber.setText(String.valueOf(number));

        tvCondition.setVisibility(View.GONE);
        spinnerCondition.setVisibility(View.VISIBLE);
        String[] conditions = {"Mint", "Near Mint", "Good", "Fair", "Poor"};
        for (int i = 0; i < conditions.length; i++) {
            if (conditions[i].equals(condition)) {
                spinnerCondition.setSelection(i);
                break;
            }
        }

        tvNotes.setVisibility(View.GONE);
        editNotes.setVisibility(View.VISIBLE);
        editNotes.setText(notes);
    }

    private void saveChanges() {
        String newName = editFunkoName.getText().toString().trim();
        String newNumberStr = editFunkoNumber.getText().toString().trim();
        String newCondition = spinnerCondition.getSelectedItem().toString();
        String newNotes = editNotes.getText().toString().trim();

        if (newName.isEmpty()) {
            android.widget.Toast.makeText(getContext(), "Name cannot be empty", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        if (newNumberStr.isEmpty()) {
            android.widget.Toast.makeText(getContext(), "Number cannot be empty", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        int newNumber;
        try {
            newNumber = Integer.parseInt(newNumberStr);
        } catch (NumberFormatException e) {
            android.widget.Toast.makeText(getContext(), "Invalid number", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(FunkoContentProvider.COL_NAME, newName);
        values.put(FunkoContentProvider.COL_NUMBER, newNumber);
        values.put(FunkoContentProvider.COL_CONDITION, newCondition);
        values.put(FunkoContentProvider.COL_NOTES, newNotes);

        String selection = "_id = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        int updatedRows = getActivity().getContentResolver().update(FunkoContentProvider.CONTENT_URI_WISHLIST, values, selection, selectionArgs);

        if (updatedRows > 0) {
            name = newName;
            number = newNumber;
            condition = newCondition;
            notes = newNotes;

            isEditMode = false;
            btnEdit.setText("Edit");

            tvFunkoName.setVisibility(View.VISIBLE);
            editFunkoName.setVisibility(View.GONE);
            tvFunkoNumber.setVisibility(View.VISIBLE);
            editFunkoNumber.setVisibility(View.GONE);
            tvCondition.setVisibility(View.VISIBLE);
            spinnerCondition.setVisibility(View.GONE);
            tvNotes.setVisibility(View.VISIBLE);
            editNotes.setVisibility(View.GONE);

            tvFunkoName.setText(name);
            tvFunkoNumber.setText("#" + number);
            tvCondition.setText(condition);
            tvNotes.setText(notes.isEmpty() ? "No notes available" : notes);

            android.widget.Toast.makeText(getContext(), "Funko Pop updated", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            android.widget.Toast.makeText(getContext(), "Failed to update", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}