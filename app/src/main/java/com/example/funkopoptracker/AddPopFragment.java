package com.example.funkopoptracker;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.funkopoptracker.database.FunkoContentProvider;
import com.example.funkopoptracker.database.RandomPriceGenerator;

public class AddPopFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_pop, container, false);

        EditText editTextName = view.findViewById(R.id.editTextName);
        EditText editTextNumber = view.findViewById(R.id.editTextNumber);
        Button buttonAdd = view.findViewById(R.id.buttonAdd);

        buttonAdd.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String numberText = editTextNumber.getText().toString();
            int number = Integer.parseInt(numberText);

            double price = RandomPriceGenerator.generatePrice(name, number);
            int rarity = RandomPriceGenerator.calculateRarity(price);

            FunkoPop newPop = new FunkoPop(name, number, rarity, null, price);

            //database stuff would happen here to save newPop
            getActivity().getContentResolver().insert(FunkoContentProvider.CONTENT_URI_OWNED, newPop.toContentValues());

            //toast
            String toastText = "FunkoPop: " + newPop.getName() + ", " + newPop.getNumber() + " added to DataBase.";
            Toast.makeText(getContext(), toastText, Toast.LENGTH_LONG).show();

            //clear the form
            editTextName.setText("");
            editTextNumber.setText("");
        });

        return view;
    }
}
