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
        int maxLenght = 0;
        if (currentHistoryPref.equals(context.getString(R.string.pref_history_key_5day))){
            maxLenght = 5;}
        else if (currentHistoryPref.equals(context.getString(R.string.pref_history_key_15day))) {
            maxLenght = 15;}
        else if (currentHistoryPref.equals(context.getString(R.string.pref_history_key_month))) {
            maxLenght= 30;}

        String[] entriesSplit = entriesString.split("\n");

        if (entriesSplit.length < maxLenght) {
            maxLenght = entriesSplit.length;}

        int reverseIterator = maxLenght - 1;
        for (int i = 0; i < maxLenght; i++) {
            String[] subsplit = entriesSplit[i].split(", ");
            resultList.add(new Entry( (float) reverseIterator-- , Float.parseFloat(subsplit[1])));
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

//    public static class GetSymbolHistory extends AsyncTask<String, Void, String > {
//        private Context context;
//        private StockRequestListener stockRequestListener;
//
//        public GetSymbolHistory(Context context, StockRequestListener stockRequestListener) {
//            this.context = context;
//            this.stockRequestListener = stockRequestListener;
//        }
//
//        @Override
//        protected String doInBackground(String... symbols) {
//            String result = "";
//            Uri uri = Contract.Quote.makeUriForStock(symbols[0]);
//            Cursor cursor = context.getContentResolver().query(uri,
//                    null, null, null, null);
//            if (cursor != null) {
//                cursor.moveToFirst();
//                result += cursor.getString(Contract.Quote.POSITION_HISTORY);
//            }
//
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            stockRequestListener.onStockRequestCompleted(result);
//        }
//    }

    public static class GetStockFromSymbol extends AsyncTask<String, Void, QuoteObject > {
        private Context context;
        private StockRequestListener stockRequestListener;

        public GetStockFromSymbol(Context context, StockRequestListener stockRequestListener) {
            this.context = context;
            this.stockRequestListener = stockRequestListener;
        }

        @Override
        protected QuoteObject doInBackground(String... symbols) {

            Uri uri = Contract.Quote.makeUriForStock(symbols[0]);
            Cursor cursor = context.getContentResolver().query(uri,
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                return cursorToQuoteObject(cursor, context);
            } else return null;
        }


        @Override
        protected void onPostExecute(QuoteObject result) {
            super.onPostExecute(result);
            stockRequestListener.onStockRequestCompleted(result);
        }
    }

    public interface StockRequestListener {
        void onStockRequestCompleted(QuoteObject result);
    }


    public static QuoteObject cursorToQuoteObject(Cursor cursor, Context context) {

        double absolute = Double.parseDouble(cursor.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
        String absoluteChange;
        if (absolute > 0) {
            absoluteChange = context.getString(R.string.plus_sign) + context.getString(R.string.dollar_sign) + absolute;
        } else absoluteChange = String.valueOf(absolute);

        double percentage = Double.parseDouble(cursor.getString(Contract.Quote.POSITION_PERCENTAGE_CHANGE));
        String percentageChange;
        if (percentage > 0) {
            percentageChange = context.getString(R.string.plus_sign) + percentage + context.getString(R.string.percent_sign);
        } else percentageChange = String.valueOf(percentage);

        return new QuoteObject(
                cursor.getString(Contract.Quote.POSITION_SYMBOL),
                context.getString(R.string.dollar_sign) + cursor.getString(Contract.Quote.POSITION_PRICE),
                absoluteChange,
                percentageChange,
                cursor.getString(Contract.Quote.POSITION_HISTORY)
                );
    }

}
