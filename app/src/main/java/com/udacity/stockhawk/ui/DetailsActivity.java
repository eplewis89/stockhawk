package com.udacity.stockhawk.ui;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.udacity.stockhawk.R;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public class DetailsActivity extends AppCompatActivity {
    private String selectedSymbol;
    private Stock selectedStock;
    private StockQuote currentQuote;
    private List<HistoricalQuote> selectedRecords;

    @BindView(R.id.textview_loading)
    TextView stockLoading;

    @BindView(R.id.textview_stock_symbol)
    TextView stockSymbol;

    @BindView(R.id.textview_stock_name)
    TextView stockName;

    @BindView(R.id.graph_stock_histogram)
    GraphView graph;

    @BindView(R.id.layout_current_price)
    LinearLayout currentPriceLayout;

    @BindView(R.id.textview_current_price)
    TextView currentPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();

        if (bundle.getString(getString(R.string.pref_stocks_key)) != null) {
            selectedSymbol = bundle.getString(getString(R.string.pref_stocks_key));

            new StockChecker().execute(selectedSymbol);
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();

        return true;
    }

    private void updateStock(Stock stock) {
        selectedStock = stock;
    }

    private void updateInterface() {
        if (selectedStock != null && selectedStock.getName() != null) {
            new GraphLoader().execute();
            new StockLoader().execute();

            stockLoading.setVisibility(View.GONE);

            stockSymbol.setText(selectedStock.getSymbol());
            stockSymbol.setVisibility(View.VISIBLE);

            stockName.setText(selectedStock.getName());
            stockName.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }

    private void updateRecords(List<HistoricalQuote> records) {
        selectedRecords = records;
    }

    private void updateGraph() {
        DataPoint[] datapoints = new DataPoint[selectedRecords.size()];

        int index = 0;

        Date startX = null;
        Date endX = null;

        int minY = 0;
        int maxY = 0;

        for (int i = selectedRecords.size() - 1; i >= 0; i--) {
            HistoricalQuote quote = selectedRecords.get(index);

            if (minY == 0 && maxY == 0) {
                minY = quote.getClose().intValue();
                maxY = quote.getClose().intValue();
            }

            if (quote.getClose().intValue() < minY) {
                minY = quote.getClose().intValue();
            }

            if (quote.getClose().intValue() >= maxY) {
                maxY = quote.getClose().intValue();
            }

            if (i == selectedRecords.size() - 1) {
                endX = quote.getDate().getTime();
            }

            if (i == selectedRecords.size() - 5) {
                startX = quote.getDate().getTime();
            }

            datapoints[i] = new DataPoint(quote.getDate().getTimeInMillis(), quote.getClose().intValue());
            index++;
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(datapoints);

        series.setColor(Color.GREEN);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(8);

        graph.getGridLabelRenderer().setHorizontalLabelsAngle(120);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setLabelHorizontalHeight(100);

        graph.addSeries(series);

        graph.getViewport().setMinX(startX.getTime());
        graph.getViewport().setMaxX(endX.getTime());
        graph.getViewport().setXAxisBoundsManual(true);

        graph.getViewport().setMinY(minY);
        graph.getViewport().setMaxY(maxY);
        graph.getViewport().setYAxisBoundsManual(true);

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScrollableY(false);

        graph.setVisibility(View.VISIBLE);

        graph.getViewport().scrollToEnd();
    }

    private void updateQuote(StockQuote quote) {
        currentQuote = quote;
    }

    private void updatePrice() {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        currentPrice.setText(format.format(currentQuote.getPrice().doubleValue()));
        currentPriceLayout.setVisibility(View.VISIBLE);
    }

    class StockChecker extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String symbol) {
            super.onPostExecute(symbol);

            updateInterface();
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

    class GraphLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            super.onPostExecute(mVoid);

            updateGraph();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                List<HistoricalQuote> records = selectedStock.getHistory();
                updateRecords(records);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class StockLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            updatePrice();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            StockQuote quote = selectedStock.getQuote();

            updateQuote(quote);

            return null;
        }
    }
}
