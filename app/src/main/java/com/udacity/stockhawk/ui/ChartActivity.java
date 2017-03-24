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

package com.udacity.stockhawk.ui;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ChartActivity
        extends AppCompatActivity
        implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.chart)
    LineChart chart;

    private String mSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        String symbol_key = getString(R.string.chart_symbol_key);
        if (intent.hasExtra(symbol_key)) {
            mSymbol = intent.getStringExtra(symbol_key);
        }

        //COMPLETED read data from content provider in a background thread
        getLoaderManager().initLoader(0, null, ChartActivity.this);

    }

    private static final String[] STOCK_HISTORY_PROJECTION = new String[]{
            Contract.Quote.COLUMN_HISTORY
    };

    private static final int COLUMN_HISTORY_INDEX = 0;


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(ChartActivity.this, Contract.Quote.makeUriForStock(mSymbol),
                STOCK_HISTORY_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        //COMPLETED display chart with stock data

        List<Entry> entries = new ArrayList<>();

        if (data.moveToFirst()) {

            try {
                String history = data.getString(COLUMN_HISTORY_INDEX);
                BufferedReader bufferedReader = new BufferedReader(new StringReader(history));

                int comma_position;
                String dateString;
                String priceString;
                String line;
                float dateInMillisFloat;
                float priceFloat;

                while ((line = bufferedReader.readLine()) != null) {

                    comma_position = line.indexOf(',');

                    dateString = line.substring(0, comma_position).trim();
                    dateInMillisFloat = Float.parseFloat(dateString);

                    priceString = line.substring(comma_position + 1, line.length()).trim();
                    priceFloat = Float.parseFloat(priceString);

                    entries.add(new Entry(dateInMillisFloat, priceFloat));
                }

                //sort the entries just in case, to avoid unexpected behaviour
                Collections.sort(entries, new EntryXComparator());

            } catch (IOException ex) {
                ex.printStackTrace();
                Timber.d("Could not read lines from history.");
                return;
            }

        } else {
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Stock price");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setColor(Color.BLACK);
        dataSet.setCircleColor(Color.RED);
        dataSet.setDrawCircles(false);

        LineData lineData = new LineData(dataSet);
        lineData.setDrawValues(true);
//        lineData.setValueFormatter(new IValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                return String.format(Locale.US, "$ %.4f", value);
//            }
//        });
        Legend legend = chart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setForm(Legend.LegendForm.CIRCLE);

        Description descr = new Description();
        descr.setText("Stock price history of " + mSymbol);
        chart.setDescription(descr);
        chart.setContentDescription(descr.getText());

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(45);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date((long) value);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
                return dateFormat.format(date);
            }
        });

        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.format(Locale.US, "$%d", Math.round(value));
            }
        });
        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false);

        chart.setData(lineData);
        chart.invalidate();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

}
