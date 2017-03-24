/*
  MIT License

  Copyright (c) 2017 Elyasin Shaladi

  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
  associated documentation files (the "Software"), to deal in the Software without restriction,
  including without limitation the rights to use, copy, modify, merge, publish, distribute,
  sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all copies or
  substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
  NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;


public class StockAppRemoteViewsService extends RemoteViewsService {

    private static final String[] STOCK_DATA_WIDGET_PROJECTION = new String[]{
            Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE
    };

    private static final int COLUMN_ID_INDEX = 0;
    private static final int COLUMN_SYMBOL_INDEX = 1;
    private static final int COLUMN_PRICE_INDEX = 2;
    private static final int COLUMN_ABSOLUTE_CHANGE_INDEX = 3;
    private static final int COLUMN_PERCENTAGE_CHANGE_INDEX = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor data = null;

            @Override
            public void onCreate() {
                //Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                Uri stocksUri = Contract.Quote.URI;
                data = getContentResolver().query(
                        stocksUri,
                        STOCK_DATA_WIDGET_PROJECTION,
                        null,
                        null,
                        null
                );

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);

                String symbol = data.getString(COLUMN_SYMBOL_INDEX);
                float price = data.getFloat(COLUMN_PRICE_INDEX);
                float absoluteChange = data.getFloat(COLUMN_ABSOLUTE_CHANGE_INDEX);
                float percentageChange = data.getFloat(COLUMN_PERCENTAGE_CHANGE_INDEX);

                views.setTextViewText(R.id.symbol, symbol);
                views.setTextViewText(R.id.price, String.valueOf(price));
                //views.setTextViewText(R.id.change, String.valueOf(absoluteChange));
                views.setTextViewText(R.id.change, String.valueOf(percentageChange));

                if (absoluteChange > 0) {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(getString(R.string.chart_symbol_key), symbol);
                views.setOnClickFillInIntent(R.id.list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) {
                    return data.getLong(COLUMN_ID_INDEX);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
