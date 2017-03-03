package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;

import java.util.List;


public class DetailActivity extends AppCompatActivity
        implements PrefUtils.HistoryCompletedListener,
        View.OnClickListener{


    private LineChart chart;
    private String currentHistoryPref;
    private String symbol;
    private AppCompatCheckBox accb_showLabels;
    private Spinner sp_timeframe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        symbol = getIntent().getStringExtra(getString(R.string.intent_key_symbol));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(symbol);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        // Chart
        chart = (LineChart)findViewById(R.id.lcv_chart);
        setupChart();

        // Interface
        accb_showLabels = (AppCompatCheckBox)findViewById(R.id.accb_showlabels);
        accb_showLabels.setOnClickListener(this);

        sp_timeframe = (Spinner) findViewById(R.id.sp_timeframe);
        sp_timeframe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String selectedSortOrder = null;
                switch (pos) {
                    case 0:
                        selectedSortOrder = getString(R.string.pref_history_val_5day);
                        break;
                    case 1:
                        selectedSortOrder = getString(R.string.pref_history_val_15day);
                        break;
                    case 2:
                        selectedSortOrder = getString(R.string.pref_history_val_month);
                        break;
                    default:
                        selectedSortOrder = getString(R.string.pref_history_val_5day);
                        break;
                }

                PreferenceManager.getDefaultSharedPreferences(DetailActivity.this)
                        .edit().putString(
                        getString(R.string.pref_history_key), selectedSortOrder)
                        .commit();

                getHistoryData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        // Get history
        getHistoryData();


    }

    private void setupChart() {
        chart.setDragEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDescription(null);
        chart.setNestedScrollingEnabled(true);

//        chart.animateX(1000);

        //x axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setGridColor(ContextCompat.getColor(this, R.color.chart_background));
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.chart_background));
        xAxis.setTextSize(8);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
//        final String[] formatValues = PrefUtils.getLast30Days(); //Todo get Last x days
//        xAxis.setValueFormatter( new IAxisValueFormatter() {
//                @Override
//                public String getFormattedValue(float value, AxisBase axis) {
//                    return formatValues[(int)value];
//                }
//            }
//        );



        // y axis
        chart.getAxisLeft().setGridColor(ContextCompat.getColor(this, R.color.chart_background));
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(this, R.color.chart_background));
        chart.getAxisLeft().setTextSize(8);
        chart.getAxisRight().setGridColor(ContextCompat.getColor(this, R.color.chart_background));
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisRight().setDrawAxisLine(false);
    }

    public void getHistoryData() {
        currentHistoryPref = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_history_key), getString(R.string.pref_history_val_5day));
        new PrefUtils.GetSymbolHistory(this, this).execute(symbol);

    }

    @Override
    public void onHistoryCompleted(String result) {

        List<Entry> entries = PrefUtils.stringToEntryList(result, currentHistoryPref, this);

        final String[] formatValues = PrefUtils.getLastXDays(entries.size()); //Todo get Last x days
        chart.getXAxis().setLabelCount(5);
        chart.getXAxis().setValueFormatter( new IAxisValueFormatter() {
                                     @Override
                                     public String getFormattedValue(float value, AxisBase axis) {
                                         return formatValues[(int)value];
                                     }
                                 }
        );


        LineDataSet dataSet = new LineDataSet(entries, null);
        dataSet.setCircleRadius(3f);
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.colorPrimaryDark ));
        dataSet.setLineWidth(1.25f);
        dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimary ));
        dataSet.setValueTextSize(10);
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.chart_highlight));
        LineData lineData = new LineData(dataSet);


        chart.setData(lineData);
        chart.notifyDataSetChanged();
        chart.invalidate();
        setLabelsStatus();

//        chart.animateX(2000);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }


    public void onSubMenuItemClicked(MenuItem item) {
        String selectedSortOrder = null;
        int id = item.getItemId();
        switch (id) {
            case R.id.action_last5:
                selectedSortOrder = getString(R.string.pref_history_val_5day);
                break;
            case R.id.action_last15:
                selectedSortOrder = getString(R.string.pref_history_val_15day);
                break;
            case R.id.action_last_month:
                selectedSortOrder = getString(R.string.pref_history_val_month);
                break;
            default:
                selectedSortOrder = getString(R.string.pref_history_val_5day);
                break;
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putString(
                getString(R.string.pref_history_key), selectedSortOrder)
                .commit();

        getHistoryData();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.accb_showlabels:
                setLabelsStatus();
                return;
        }
    }

    private void setLabelsStatus() {
        chart.getLineData().setDrawValues(accb_showLabels.isChecked());
        chart.invalidate();
    }
}
