package com.github.hintofbasil.hodl.database.patches;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by will on 25/10/17.
 */

public class AddTablePatch implements Patch {

    private String createSQL, deleteSQL;

    public AddTablePatch(String createSQL, String deleteSQL) {
        this.createSQL = createSQL;
        this.deleteSQL = deleteSQL;
    }

    @Override
    public void apply(SQLiteDatabase sqLiteDatabase) {
        Log.d("Patch", "Applying: " + createSQL);
        sqLiteDatabase.execSQL(createSQL);
    }

    @Override
    public void revert(SQLiteDatabase sqLiteDatabase) {
        Log.d("Patch", "Reverting: " + deleteSQL);
        sqLiteDatabase.execSQL(deleteSQL);
    }
}
