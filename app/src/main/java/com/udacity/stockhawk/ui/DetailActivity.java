package com.udacity.stockhawk.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.data.QuoteObject;

import java.util.List;


public class DetailActivity extends AppCompatActivity
        implements PrefUtils.StockRequestListener,
        View.OnClickListener{


    private LineChart chart;
    private String currentHistoryPref;
    private String symbol;
    private AppCompatCheckBox accb_showLabels;
    private QuoteObject currentObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        symbol = getIntent().getStringExtra(getString(R.string.intent_key_symbol));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(symbol);
        }


        // Chart
        chart = (LineChart)findViewById(R.id.lcv_chart);
        setupChart();

        // Interface
        accb_showLabels = (AppCompatCheckBox)findViewById(R.id.accb_showlabels);
        accb_showLabels.setOnClickListener(this);

        final Spinner sp_timeframe = (Spinner) findViewById(R.id.sp_timeframe);
        sp_timeframe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String selectedSortOrder;
                switch (pos) {
                    case 0:
                        selectedSortOrder = getString(R.string.pref_history_key_5day);
                        break;
                    case 1:
                        selectedSortOrder = getString(R.string.pref_history_key_15day);
                        break;
                    case 2:
                        selectedSortOrder = getString(R.string.pref_history_key_month);
                        break;
                    default:
                        selectedSortOrder = getString(R.string.pref_history_key_5day);
                        break;
                }

                PreferenceManager.getDefaultSharedPreferences(DetailActivity.this)
                        .edit().putString(
                        getString(R.string.pref_history_key), selectedSortOrder)
                        .commit();

                getStockData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        // Get history
        getStockData();


    }

    private void setAppBarLayout() {
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        assert appBarLayout != null;
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    String title = currentObject.getSymbol() + " "
                            + currentObject.getPrice() + " / "
                            + currentObject.getAbsoluteChange() + " / "
                            + currentObject.getPercentageChange();
                    collapsingToolbarLayout.setTitle(title);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(currentObject.getSymbol());
                    isShow = false;
                }
            }
        });

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            appBarLayout.setExpanded(false);
        }
    }

    private void setupChart() {
        chart.setTouchEnabled(false);
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




        // y axis
        chart.getAxisLeft().setGridColor(ContextCompat.getColor(this, R.color.chart_background));
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(this, R.color.chart_background));
        chart.getAxisLeft().setTextSize(8);
        chart.getAxisRight().setGridColor(ContextCompat.getColor(this, R.color.chart_background));
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisRight().setDrawAxisLine(false);
    }

    public void getStockData() {
        currentHistoryPref = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_history_key), getString(R.string.pref_history_key_5day));
        new PrefUtils.GetStockFromSymbol(this, this).execute(symbol);

    }

    @Override
    public void onStockRequestCompleted(QuoteObject result) {
        currentObject = result;
        setAppBarLayout();

        populateChart(result.getHistory());

        ((TextView)findViewById(R.id.tv_detail_price))
                .setText(result.getPrice());





        ((TextView)findViewById(R.id.tv_detail_change))
                .setText(
                        result.getAbsoluteChange() +
                        " / " +
                        result.getPercentageChange()
                );


    }

    private void populateChart(String history) {
        List<Entry> entries = PrefUtils.stringToEntryList(history, currentHistoryPref, this);

        final String[] formatValues = PrefUtils.getLastXDays(entries.size());
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.accb_showlabels:
                setLabelsStatus();
                return;
            default: return;
        }
    }

    private void setLabelsStatus() {
        chart.getLineData().setDrawValues(accb_showLabels.isChecked());
        chart.invalidate();
    }
}
