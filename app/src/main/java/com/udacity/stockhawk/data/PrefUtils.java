package com.udacity.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.github.mikephil.charting.data.Entry;
import com.udacity.stockhawk.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PrefUtils {

    private PrefUtils() {
    }

    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        boolean initialized = prefs.getBoolean(initializedKey, false);

        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            editor.putStringSet(stocksKey, defaultStocks);
            editor.apply();
            return defaultStocks;
        }
        return prefs.getStringSet(stocksKey, new HashSet<String>());

    }

    private static void editStockPref(Context context, String symbol, Boolean add) {
        String key = context.getString(R.string.pref_stocks_key);
        Set<String> stocks = getStocks(context);

        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, stocks);
        editor.apply();
    }

    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);
    }

    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }

    public static List<Entry> stringToEntryList(String entriesString, String currentHistoryPref, Context context) {
        List<Entry> resultList = new ArrayList<>(0);
        int i = -1;
        if (currentHistoryPref.equals(context.getString(R.string.pref_history_val_5day))){
            i = 4;}
        else if (currentHistoryPref.equals(context.getString(R.string.pref_history_val_15day))) {
            i = 14;}
        else if (currentHistoryPref.equals(context.getString(R.string.pref_history_val_month))) {
            i= 29;}

        String[] entriesSplit = entriesString.split("\n");
        if (entriesSplit.length < i) {
            i = entriesSplit.length;}
        for (; i >= 0; i--) {
            String[] subsplit = entriesSplit[i].split(", ");
            resultList.add(new Entry( (float) i , Float.parseFloat(subsplit[1])));
        }


        Collections.reverse(resultList);
        return resultList;
    }

    public static String[] getLastXDays(int days) {
        ArrayList<String> workStrings = new ArrayList<>(0);
        for (; days >= 0; days--) {
            DateFormat dateFormat = new SimpleDateFormat("MM dd");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -days);
            String entryDate = dateFormat.format(calendar.getTime());
            workStrings.add(entryDate);
        }
        return  workStrings.toArray(new String[workStrings.size()]);

    }

    public static class GetSymbolHistory extends AsyncTask<String, Void, String > {
        private Context context;
        private HistoryCompletedListener historyCompletedListener;

        public GetSymbolHistory(Context context, HistoryCompletedListener historyCompletedListener) {
            this.context = context;
            this.historyCompletedListener = historyCompletedListener;
        }

        @Override
        protected String doInBackground(String... symbols) {
            String result = "";
            Uri uri = Contract.Quote.makeUriForStock(symbols[0]);
            Cursor cursor = context.getContentResolver().query(uri,
                    null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                result += cursor.getString(Contract.Quote.POSITION_HISTORY);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            historyCompletedListener.onHistoryCompleted(result);
        }
    }

    public interface HistoryCompletedListener {
        void onHistoryCompleted(String result);
    }

}
