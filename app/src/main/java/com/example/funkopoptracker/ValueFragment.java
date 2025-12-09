package com.example.funkopoptracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.funkopoptracker.database.FunkoContentProvider;
import com.example.funkopoptracker.database.RandomPriceGenerator;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ValueFragment extends Fragment {

    private TextView totalValueText;
    private TextView dayText;
    private ListView listView;
    private LineChart chart;
    private List<FunkoPop> funkoPops = new ArrayList<>();
    private List<Double> prices = new ArrayList<>();
    private int currentDay = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_value, container, false);

        totalValueText = view.findViewById(R.id.totalValueText);
        dayText = view.findViewById(R.id.dayText);
        listView = view.findViewById(R.id.valueListView);
        chart = view.findViewById(R.id.priceChart);
        Button advanceDayButton = view.findViewById(R.id.advanceDayButton);

        //load pops from db
        Cursor cursor = getActivity().getContentResolver().query(FunkoContentProvider.CONTENT_URI_OWNED, null, null, null, null);
        funkoPops.addAll(FunkoPop.allFromCursor(cursor));

        //get current day from db
        Cursor dayCursor = getActivity().getContentResolver().query(
            FunkoContentProvider.CONTENT_URI_PRICE_HISTORY, new String[]{"MAX(timestamp)"}, null, null, null);
        if (dayCursor.moveToFirst()) currentDay = dayCursor.getInt(0);
        dayCursor.close();

        loadPrices();
        updateDisplay();



        advanceDayButton.setOnClickListener(v -> {
            if (!funkoPops.isEmpty()) advanceDay();
        });

        return view;
    }

    private void loadPrices() {
        prices.clear();

        for (int i = 0; i < funkoPops.size(); i++) {
            FunkoPop pop = funkoPops.get(i);

            if (currentDay == 0) {
                prices.add(pop.getPrice());
            } else {
                //try to get from history
                Cursor cursor = getActivity().getContentResolver().query(
                    FunkoContentProvider.CONTENT_URI_PRICE_HISTORY, null,
                    "funko_id = " + pop.getId() + " AND timestamp = " + currentDay, null, null);

                if (cursor.moveToFirst()) {
                    prices.add(cursor.getDouble(cursor.getColumnIndexOrThrow("PRICE")));
                } else {
                    prices.add(pop.getPrice());
                }
                cursor.close();
            }
        }
    }

    //updates all funko pops in collection with a new price based on generatePriceDelta().
    private void advanceDay() {

        currentDay++; //increment day

        for (int i = 0; i < funkoPops.size(); i++) {
            FunkoPop pop = funkoPops.get(i);
            double previousPrice = prices.get(i);

            //generates new price + pricedelta
            double newPrice = RandomPriceGenerator.generatePriceDelta(previousPrice, pop.getName(), pop.getNumber(), currentDay);

            //save to history
            ContentValues values = new ContentValues();
            values.put("funko_id", pop.getId());
            values.put("PRICE", newPrice);
            values.put("timestamp", currentDay);
            getActivity().getContentResolver().insert(FunkoContentProvider.CONTENT_URI_PRICE_HISTORY, values);

            //update main table
            ContentValues updateValues = new ContentValues();
            updateValues.put(FunkoContentProvider.COL_PRICE, newPrice);
            getActivity().getContentResolver().update(
                FunkoContentProvider.CONTENT_URI_OWNED, updateValues, "_id = " + pop.getId(), null);

            prices.set(i, newPrice);
        }

        updateDisplay();
    }

    private void updateDisplay() {
        dayText.setText("Day " + currentDay);

        double totalValue = 0;
        for (Double price : prices) {
            totalValue += price;
        }
        totalValueText.setText("Total Value: $" + String.format("%.2f", totalValue));

        ArrayAdapter<FunkoPop> adapter = new ArrayAdapter<FunkoPop>(
            getContext(),
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            funkoPops
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView popNameText = view.findViewById(android.R.id.text1);
                TextView popNumberText = view.findViewById(android.R.id.text2);
                FunkoPop funkoPop = getItem(position);
                double price = prices.get(position);
                popNameText.setText(funkoPop.getName());
                popNumberText.setText(funkoPop.getNumber() + " - $" + String.format("%.2f", price));
                return view;
            }
        };

        listView.setAdapter(adapter);
        updateChart();
    }

    private void updateChart() {
        List<Entry> entries = new ArrayList<>();

        //day 0 = sum of base prices
        double day0Total = 0;
        for (FunkoPop pop : funkoPops) {
            day0Total += pop.getPrice();
        }
        entries.add(new Entry(0, (float) day0Total));

        //days 1 to currentDay from price_history
        for (int day = 1; day <= currentDay; day++) {
            Cursor cursor = getActivity().getContentResolver().query(
                FunkoContentProvider.CONTENT_URI_PRICE_HISTORY, new String[]{"SUM(PRICE)"},
                "timestamp = " + day, null, null);
            if (cursor.moveToFirst()) {
                entries.add(new Entry(day, cursor.getFloat(0)));
            }
            cursor.close();
        }

        LineDataSet dataSet = new LineDataSet(entries, "Total Value");
        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }
}
