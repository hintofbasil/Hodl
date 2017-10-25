package com.github.hintofbasil.hodl.database.patches;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Method;

/**
 * Created by will on 25/10/17.
 */

public class UpdateColumnsPatch implements Patch {

    private String tableName;
    private String newCreateSQL;
    private Class oldObject;
    private String[] oldProjection;
    private Class newObject;

    public UpdateColumnsPatch(String tableName, String newCreateSQL, Class oldObject, String[] oldProjection, Class newObject) {
        this.tableName = tableName;
        this.newCreateSQL = newCreateSQL;
        this.oldObject = oldObject;
        this.oldProjection = oldProjection;
        this.newObject = newObject;
    }

    @Override
    public void apply(SQLiteDatabase sqLiteDatabase) throws Exception {
        // Move table
        String tmpTableName = tableName + "_bck";
        String sqlRenameTable = "ALTER TABLE " + tableName + " RENAME TO " + tmpTableName;
        sqLiteDatabase.execSQL(sqlRenameTable);

        // Create new table
        sqLiteDatabase.execSQL(newCreateSQL);

        Method builder = oldObject.getMethod("buildFromCursor", Cursor.class);
        Method saver = newObject.getMethod("addToDatabase", SQLiteDatabase.class);

        // Query temporary table
        Cursor cursor = sqLiteDatabase.query(
                tmpTableName,
                oldProjection,
                null,
                null,
                null,
                null,
                null
        );

        // Save data into new table
        while(cursor.moveToNext()) {
            Object old = builder.invoke(null, cursor);
            saver.invoke(old, sqLiteDatabase);
        }
        cursor.close();

        // Delete temporary table
        String deleteTmpTable = "DROP TABLE " + tmpTableName;
        sqLiteDatabase.execSQL(deleteTmpTable);
    }

    @Override
    public void revert(SQLiteDatabase sqLiteDatabase) {

    }
}

