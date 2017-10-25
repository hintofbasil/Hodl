package com.github.hintofbasil.hodl.database.patches;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by will on 25/10/17.
 */

public interface Patch {

    void apply(SQLiteDatabase sqLiteDatabase);
    void revert(SQLiteDatabase sqLiteDatabase);

}
