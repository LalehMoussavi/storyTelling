package android;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;


public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String database_name = "Tour.db";
    private String table_name = "stops";
    private static final int database_version = 1;
    private String KEY_ID = "id", NAME ="Name", XCOORD = "points_coorx", YCOORD = "points_coory", STORY = "story";
    private String database_create_statement =
            " CREATE TABLE " + table_name + " ( "+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + NAME + " TEXT NOT NULL, " + XCOORD + " TEXT NOT NULL, " + YCOORD + " TEXT NOT NULL, " + STORY + " TEXT NOT NULL); ";

    public MySQLiteOpenHelper(Context context)
    {
        super(context, database_name, null, database_version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        System.err.println("Is it Null?"+ database_create_statement);
        sqLiteDatabase.execSQL(database_create_statement);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



}

