package com.github.hintofbasil.hodl.database.patches;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.hintofbasil.hodl.database.objects.DbObject;

import java.lang.reflect.Method;

/**
 * Created by will on 25/10/17.
 */

public class UpdateColumnsPatch implements Patch {

    private String tableName;
    private String oldCreateSQL;
    private String newCreateSQL;
    private String[] oldProjection;
    private String[] newProjection;
    private Class oldObject;
    private Class newObject;

    public UpdateColumnsPatch(String tableName,
                              String oldCreateSQL,
                              String newCreateSQL,
                              String[] oldProjection,
                              String[] newProjection,
                              Class oldObject,
                              Class newObject) {
        this.tableName = tableName;
        this.oldCreateSQL = oldCreateSQL;
        this.newCreateSQL = newCreateSQL;
        this.oldProjection = oldProjection;
        this.newProjection = newProjection;
        this.oldObject = oldObject;
        this.newObject = newObject;
    }

    @Override
    public void apply(SQLiteDatabase sqLiteDatabase) throws Exception {
        doMigration(sqLiteDatabase, newCreateSQL, oldObject, oldProjection, "upgrade");
    }

    @Override
    public void revert(SQLiteDatabase sqLiteDatabase) throws Exception {
        doMigration(sqLiteDatabase, oldCreateSQL, newObject, newProjection, "downgrade");
    }

    private void doMigration(SQLiteDatabase sqLiteDatabase,
                             String createSQL,
                             Class oldObject,
                             String[] projection,
                             String action) throws Exception {

        // Move table
        String tmpTableName = tableName + "_bck";
        String sqlRenameTable = "ALTER TABLE " + tableName + " RENAME TO " + tmpTableName;
        sqLiteDatabase.execSQL(sqlRenameTable);

        // Create new table
        sqLiteDatabase.execSQL(createSQL);

        Method builder = oldObject.getMethod("buildFromCursor", Cursor.class);

        // Query temporary table
        Cursor cursor = sqLiteDatabase.query(
                tmpTableName,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        // Save data into new table
        while(cursor.moveToNext()) {
            DbObject old = (DbObject) builder.invoke(null, cursor);
            DbObject upgraded = (DbObject) old.getClass().getMethod(action).invoke(old);
            upgraded.addToDatabase(sqLiteDatabase);
        }
        cursor.close();

        // Delete temporary table
        String deleteTmpTable = "DROP TABLE " + tmpTableName;
        sqLiteDatabase.execSQL(deleteTmpTable);
    }
}

