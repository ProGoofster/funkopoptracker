package com.example.funkopoptracker;

import android.content.Context;
import android.util.Base64;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;



//ARCHIVED - no longer implementing into app due to complexity.



public class EbayApi {
    private RequestQueue queue;

    public EbayApi(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    // Callback to return price back to ValueFragment
    // Interface required because async - can't return directly when result comes later
    public interface PriceCallback {
        void onResult(double price);
    }

    // Fetches current eBay price for a Funko Pop
    public void getPrice(String name, int number, PriceCallback callback) {
        // Step 1: Get OAuth token from eBay
        // encode in Base64 required by OAuth spec
        String credentials = BuildConfig.EBAY_CLIENT_ID + ":" + BuildConfig.EBAY_CLIENT_SECRET;
        String encoded = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        // POST to get access token
        // Async required by Android - network on main thread crashes app
        // StringRequest(int method,
        //               String url,
        //               Response.Listener<String> listener,      → response -> {...}
        //               Response.ErrorListener errorListener)    → error -> {...}
        StringRequest tokenRequest = new StringRequest(
            Request.Method.POST,
            "https://api.sandbox.ebay.com/identity/v1/oauth2/token",
            response -> {
                try {
                    String token = new JSONObject(response).getString("access_token");

                    // Step 2: Search eBay with the token
                    String searchUrl = "https://api.sandbox.ebay.com/buy/browse/v1/item_summary/search?q=Funko Pop " + name + " " + number;

                    JsonObjectRequest priceRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        searchUrl,
                        null,
                        priceResponse -> { // volley - executes on response
                            try {
                                double price = calculateAveragePrice(priceResponse);
                                callback.onResult(price);
                            } catch (Exception e) {
                                callback.onResult(0.0);
                            }
                        },
                        error -> callback.onResult(0.0)
                    ) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            // Bearer token proves we're authorized to use the API
                            headers.put("Authorization", "Bearer " + token);
                            headers.put("X-EBAY-C-MARKETPLACE-ID", "EBAY_US");
                            return headers;
                        }
                    };

                    queue.add(priceRequest);
                } catch (Exception e) {
                    callback.onResult(0.0);
                }
            },
            error -> callback.onResult(0.0)
        ) {
            // Can't do this with lambdas because they only work for interfaces with 1 method
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Basic auth with encoded credentials to prove who we are
                headers.put("Authorization", "Basic " + encoded);
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }

            @Override
            public byte[] getBody() {
                // Tells eBay to give a client credentials token with API access
                return "grant_type=client_credentials&scope=https://api.ebay.com/oauth/api_scope".getBytes();
            }
        };

        queue.add(tokenRequest);
    }

    // parse json and calculate average price from all listings
    private double calculateAveragePrice(JSONObject response) throws Exception {
        if (!response.has("itemSummaries")) return 0.0;

        JSONArray items = response.getJSONArray("itemSummaries");
        if (items.length() == 0) return 0.0;

        double total = 0.0;
        int count = 0;

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item.has("price")) {
                String priceValue = item.getJSONObject("price").getString("value");
                total += Double.parseDouble(priceValue);
                count++;
            }
        }

        // Return average if we found prices, otherwise 0.0 to indicate no data
        if (count > 0) {
            return total / count;
        } else {
            return 0.0;
        }
    }
}
