package com.udacity.stockhawk.ui;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.support.annotation.Nullable;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Erin Lewis on 3/16/2017.
 */

public class StockWidgetIntentService extends RemoteViewsService {
    private static final String[] STOCK_COLUMNS = {
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE
    };

    private static final int INDEX_SYMBOL = 0;
    private static final int INDEX_PRICE = 1;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Timber.d("stockwidgetintentservice ongetviewfactory");

        return new RemoteViewsFactory() {

            private Cursor data = null;

            @Override
            public void onCreate() {
                Timber.d("stockwidgetintentservice oncreate");
                // ignore
            }

            @Override
            public void onDataSetChanged() {
                Timber.d("stockwidgetintentservice ondatasetchanged");
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                Uri stockQuoteURI = Contract.Quote.URI;

                data = getContentResolver().query(stockQuoteURI, STOCK_COLUMNS, null, null, Contract.Quote.COLUMN_SYMBOL + " ASC");

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                Timber.d("stockwidgetintentservice ondestroy");
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                Timber.d("stockwidgetintentservice getcount");
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                Timber.d("stockwidgetintentservice getviewat " + position);
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.stock_widget_list_item);

                String stockSymbol = data.getString(INDEX_SYMBOL);
                views.setTextViewText(R.id.widget_tv_symbol, stockSymbol);

                DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                String stockPrice = dollarFormat.format(data.getFloat(INDEX_PRICE));
                views.setTextViewText(R.id.widget_tv_price, stockPrice);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                Timber.d("stockwidgetintentservice getloadingview");
                return new RemoteViews(getPackageName(), R.layout.stock_widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
