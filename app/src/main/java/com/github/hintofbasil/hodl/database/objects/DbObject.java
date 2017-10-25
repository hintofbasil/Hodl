package com.github.hintofbasil.hodl.database.objects;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by will on 25/10/17.
 */

public interface DbObject {

    DbObject upgrade();
    DbObject downgrade();

    long addToDatabase(SQLiteDatabase database);
    int updateDatabase(SQLiteDatabase database, String... toUpdate);

}