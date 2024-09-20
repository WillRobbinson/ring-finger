package com.brain.ringconnector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SensorData.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SENSOR_DATA = "sensor_data";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_ACCEL_X = "accel_x";
    public static final String COLUMN_ACCEL_Y = "accel_y";
    public static final String COLUMN_ACCEL_Z = "accel_z";
    public static final String COLUMN_GYRO_X = "gyro_x";
    public static final String COLUMN_GYRO_Y = "gyro_y";
    public static final String COLUMN_GYRO_Z = "gyro_z";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_SENSOR_DATA + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_TIMESTAMP
            + " real not null, " + COLUMN_ACCEL_X + " real not null, "
            + COLUMN_ACCEL_Y + " real not null, " + COLUMN_ACCEL_Z
            + " real not null, " + COLUMN_GYRO_X + " real not null, "
            + COLUMN_GYRO_Y + " real not null, " + COLUMN_GYRO_Z + " real not null);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR_DATA);
        onCreate(db);
    }
}