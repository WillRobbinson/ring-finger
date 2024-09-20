package com.brain.ringconnector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class SensorDataSource {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] allColumns = { DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_TIMESTAMP, DatabaseHelper.COLUMN_ACCEL_X,
            DatabaseHelper.COLUMN_ACCEL_Y, DatabaseHelper.COLUMN_ACCEL_Z,
            DatabaseHelper.COLUMN_GYRO_X, DatabaseHelper.COLUMN_GYRO_Y,
            DatabaseHelper.COLUMN_GYRO_Z };

    public SensorDataSource(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void insertSensorData(SensorData data) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TIMESTAMP, data.getTimestamp());
        values.put(DatabaseHelper.COLUMN_ACCEL_X, data.getAccel_x());
        values.put(DatabaseHelper.COLUMN_ACCEL_Y, data.getAccel_y());
        values.put(DatabaseHelper.COLUMN_ACCEL_Z, data.getAccel_z());
        values.put(DatabaseHelper.COLUMN_GYRO_X, data.getGyro_x());
        values.put(DatabaseHelper.COLUMN_GYRO_Y, data.getGyro_y());
        values.put(DatabaseHelper.COLUMN_GYRO_Z, data.getGyro_z());

        database.insert(DatabaseHelper.TABLE_SENSOR_DATA, null, values);
    }

    public List<SensorData> getAllSensorData() {
        List<SensorData> sensorDataList = new ArrayList<>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_SENSOR_DATA,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SensorData sensorData = cursorToSensorData(cursor);
            sensorDataList.add(sensorData);
            cursor.moveToNext();
        }
        cursor.close();
        return sensorDataList;
    }

    private SensorData cursorToSensorData(Cursor cursor) {
        SensorData sensorData = new SensorData();
        sensorData.setId(cursor.getLong(0));
        sensorData.setTimestamp(cursor.getDouble(1));
        sensorData.setAccel_x(cursor.getDouble(2));
        sensorData.setAccel_y(cursor.getDouble(3));
        sensorData.setAccel_z(cursor.getDouble(4));
        sensorData.setGyro_x(cursor.getDouble(5));
        sensorData.setGyro_y(cursor.getDouble(6));
        sensorData.setGyro_z(cursor.getDouble(7));
        return sensorData;
    }
}
