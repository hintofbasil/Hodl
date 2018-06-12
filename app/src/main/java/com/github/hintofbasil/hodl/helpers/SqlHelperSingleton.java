package com.github.hintofbasil.hodl.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.hintofbasil.hodl.database.DbHelper;

// Idea taken from http://touchlabblog.tumblr.com/post/24474750219/single-sqlite-connection
// Appears to still be relevant
public class SqlHelperSingleton {

    private static SQLiteDatabase instance;

    public static synchronized SQLiteDatabase getDatabase(Context context) {
        if (instance == null) {
            instance = new DbHelper(context).getWritableDatabase();
        }
        return instance;
    }

}
