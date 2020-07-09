package com.example.findmedevice.connection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.findmedevice.utils.ConstantSQLite;

public class ConectionSQLite extends SQLiteOpenHelper {

    public ConectionSQLite(Context context,String name,SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ConstantSQLite.CREAR_TABLA_PERSONA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ConstantSQLite.TABLA_PERSONA);
    }

    public void deleteAll(SQLiteDatabase db) {
        db.delete(ConstantSQLite.TABLA_PERSONA, null, null);
    }
}
