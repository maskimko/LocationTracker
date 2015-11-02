package android.ua.pp.msk.locationtry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by maskimko on 11/2/15.
 */
public class LocationDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Location.db";


    private static final String TEXT_TYPE = " TEXT";
    private static final String LOC_TYPE = " REAL";
    private static final String COMMA_SEP = ", ";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
            LocationEntry.TABLE_NAME + " ( " + LocationEntry._ID + " INTEGER PRIMARY KEY, "
            + LocationEntry.COLUMN_NAME_LATITUDE + LOC_TYPE + COMMA_SEP + LocationEntry.COLUMN_NAME_LONGITUDE
            + LOC_TYPE + COMMA_SEP + LocationEntry.COLUMN_NAME_ALTITUDE + LOC_TYPE + COMMA_SEP
            + LocationEntry.COLUMN_NAME_ACCURACY + LOC_TYPE + COMMA_SEP
            + LocationEntry.COLUMN_NAME_TIMESTAMP + TEXT_TYPE + COMMA_SEP
            + LocationEntry.COLUMN_NAME_DEV_ID + " INTEGER );";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME +" ;";




    public static abstract class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "LocationArchive";
        public static final String COLUMN_NAME_LATITUDE = "lat";
        public static final String COLUMN_NAME_LONGITUDE = "lon";
        public static final String COLUMN_NAME_ALTITUDE = "alt";
        public static final String COLUMN_NAME_ACCURACY = "acc";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_DEV_ID = "device_id";
        //Do not insert null value raws into the database;
        public static final String COLUMN_NAME_NULLABLE = null;
    }


    public LocationDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       onUpgrade(db, oldVersion, newVersion);
    }
}
