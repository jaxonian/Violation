package edu.nyu.scps.violation;


import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class Helper extends SQLiteAssetHelper {

    public Helper(Context context) {
        super(context, "restaurants.db", null, 1);
    }
}
