package com.udacity.stockhawk.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

/**
 * Created by Manolo on 09/03/2017.
 */

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private Intent intent;
    private Cursor mCursor;

    public WidgetDataProvider(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    @Override
    public void onCreate() {
        loadStocksCursor();
    }

    @Override
    public void onDataSetChanged() {
        loadStocksCursor();
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        // check for data in position
        if (!mCursor.moveToPosition(position)) {
            return null;
        }

        // inflate
        RemoteViews view = new RemoteViews(
                context.getPackageName(),
                R.layout.item_widget_stocks
        );

        // populate
        String symbol = mCursor.getString(Contract.Quote.POSITION_SYMBOL);
        view.setTextViewText(R.id.tv_widget_item_symbol, symbol);
        view.setTextViewText(R.id.tv_widget_item_price,
                mCursor.getString(Contract.Quote.POSITION_PRICE));

        float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        if (percentageChange > 0) {
            view.setTextViewText(R.id.tv_widget_item_change,
                    context.getString(R.string.plus_sign) + percentageChange);
        } else {
            view.setTextViewText(R.id.tv_widget_item_change, String.valueOf(percentageChange));
        }

        // fill onClick intent template (CollectionWidget.class) with actual data
        final Intent fillIntent = new Intent();
        fillIntent.putExtra(context.getString(R.string.intent_key_symbol), symbol);
        view.setOnClickFillInIntent(R.id.ll_item_widget_stock, fillIntent);

        // return the set up view
        return view;

    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        // number of different item views
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }



    private void loadStocksCursor() {
        ContentResolver resolver = context.getContentResolver();
        mCursor = resolver.query(
                Contract.Quote.URI,
                null, null, null,
                Contract.Quote.COLUMN_SYMBOL);
    }


}
