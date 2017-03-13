package com.udacity.stockhawk.ui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.io.IOException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    class StockChecker extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String symbol) {
            super.onPostExecute(symbol);

            updateUser();
        }

        @Override
        protected String doInBackground(String... symbol) {
            try {
                Stock stock = YahooFinance.get(symbol[0]);
                updateStock(stock);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private void updateStock(Stock stock) {

    }

    private void updateUser() {

    }
}
