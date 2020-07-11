package com.example.findmedevice.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import com.example.findmedevice.connection.ConectionSQLite;
import com.example.findmedevice.models.Person;
import com.example.findmedevice.models.Smartphone;

public class ConstantSQLite {
    public static final String BASE_DATOS = "db_findme_device";
    public static final String TABLA_PERSONA = "person";
    public static final String TABLA_SMARTPHONE = "smartphone";
    public static final String CAMPO_ID = "id";
    public static final String CAMPO_NOMBRE = "nombre";
    public static final String CAMPO_APELLIDO = "apellido";
    public static final String CREAR_TABLA_PERSONA = "CREATE TABLE "+TABLA_PERSONA+" ("+CAMPO_ID+" INTEGER, "+CAMPO_NOMBRE+" TEXT, " +
            ""+CAMPO_APELLIDO+" TEXT)";

    public static final String CREAR_TABLA_SMARTPHONE = "CREATE TABLE "+TABLA_SMARTPHONE+" ("+CAMPO_ID+" INTEGER)";


    public static void RegisterPersonSQL(Person person, Context context) {
        ConectionSQLite conSQL;
        conSQL = new ConectionSQLite(context, ConstantSQLite.BASE_DATOS, null, 1);
        SQLiteDatabase db = conSQL.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ConstantSQLite.CAMPO_ID, person.getId());
        values.put(ConstantSQLite.CAMPO_NOMBRE, person.getName());
        values.put(ConstantSQLite.CAMPO_APELLIDO, person.getLastName());
        db.insert(ConstantSQLite.TABLA_PERSONA, null, values);
        db.close();
    }

    public static Person ConsultarDatosPerson(Context context) {
        Person person = new Person();
        ConectionSQLite conSQLite = new ConectionSQLite(context, ConstantSQLite.BASE_DATOS, null, 1);
        SQLiteDatabase db = conSQLite.getReadableDatabase();
        try {
            Cursor cursorUser = db.rawQuery("SELECT "+ConstantSQLite.CAMPO_ID+", "+ConstantSQLite.CAMPO_NOMBRE+", "+ConstantSQLite.CAMPO_APELLIDO+" FROM "+ConstantSQLite.TABLA_PERSONA, null);
            cursorUser.moveToFirst();
            person.setId(cursorUser.getString(cursorUser.getColumnIndex(CAMPO_ID)));
            person.setName(cursorUser.getString(cursorUser.getColumnIndex(CAMPO_NOMBRE)));
            person.setLastName(cursorUser.getString(cursorUser.getColumnIndex(CAMPO_APELLIDO)));
            cursorUser.close();
        }catch (Exception e){
            Toast.makeText(context, "No resulto Person", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return person;
    }

    public static void BorrarDB(Context context){
        ConectionSQLite sqLite = new ConectionSQLite(context, ConstantSQLite.BASE_DATOS, null, 1);
        SQLiteDatabase db = sqLite.getWritableDatabase();
        sqLite.deleteAll(db);
        db.close();
    }

    public static void RegisterSmartphoneSQL(Smartphone smartphone, Context context) {
        ConectionSQLite conSQL;
        conSQL = new ConectionSQLite(context, ConstantSQLite.BASE_DATOS, null, 1);
        SQLiteDatabase db = conSQL.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ConstantSQLite.CAMPO_ID, smartphone.getId());
        db.insert(ConstantSQLite.TABLA_SMARTPHONE, null, values);
        db.close();
    }

    public static Smartphone ConsultarSmartphone(Context context) {
        Smartphone smartphone = new Smartphone();
        ConectionSQLite conSQLite = new ConectionSQLite(context, ConstantSQLite.BASE_DATOS, null, 1);
        SQLiteDatabase db = conSQLite.getReadableDatabase();
        try {
            Cursor cursorSmartphone = db.rawQuery("SELECT "+ConstantSQLite.CAMPO_ID+" FROM "+ConstantSQLite.TABLA_SMARTPHONE, null);
            cursorSmartphone.moveToFirst();
            smartphone.setId(cursorSmartphone.getString(cursorSmartphone.getColumnIndex(CAMPO_ID)));
            cursorSmartphone.close();
        }catch (Exception e){
            Toast.makeText(context, "No resulto Smartphone", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return smartphone;
    }
}
