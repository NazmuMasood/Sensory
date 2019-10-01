package com.example.sensory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    SQLiteDatabase db;

    public static final String DATABASE_NAME = "sensors.db";
    public static final String TABLE_NAME = "sensor_data";
    public static final String DATA_INDEX = "`index`";
    public static final String USER_ACCELERATION_X = "userAccelerationX";
    public static final String USER_ACCELERATION_Y = "userAccelerationY";
    public static final String USER_ACCELERATION_Z = "userAccelerationZ";
    public static final String ROTATION_RATE_X = "rotationRateX";
    public static final String ROTATION_RATE_Y = "rotationRateY";
    public static final String ROTATION_RATE_Z = "rotationRateZ";
    public static final String GRAVITY_X = "gravityX";
    public static final String GRAVITY_Y = "gravityY";
    public static final String GRAVITY_Z = "gravityZ";
    public static final String ATTITUDE_AZIMUTH = "attitudeAzimuth";
    public static final String ATTITUDE_PITCH = "attitudePitch";
    public static final String ATTITUDE_ROLL = "attitudeRoll";


    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table " + TABLE_NAME + "(DATA_INDEX INTEGER PRIMARY KEY AUTOINCREMENT," +
                "USER_ACCELERATION_X REAL,USER_ACCELERATION_Y REAL,USER_ACCELERATION_Z REAL," +
                "ROTATION_RATE_X REAL,ROTATION_RATE_Y REAL,ROTATION_RATE_Z REAL," +
                "GRAVITY_X REAL,GRAVITY_Y REAL, GRAVITY_Z REAL," +
                "ATTITUDE_AZIMUTH REAL,ATTITUDE_PITCH REAL,ATTITUDE_ROLL REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    //WRITE SENSOR DATA TO DATABASE
    public boolean writeDataToDb(Float[] dataArray) {

        for (int i = 0; i < dataArray.length; i++){
            ContentValues contentValues = new ContentValues();
            contentValues.put(USER_ACCELERATION_X,dataArray[0]);
            contentValues.put(USER_ACCELERATION_Y,dataArray[1]);
            contentValues.put(USER_ACCELERATION_Z,dataArray[2]);

            contentValues.put(ROTATION_RATE_X,dataArray[3]);
            contentValues.put(ROTATION_RATE_Y,dataArray[4]);
            contentValues.put(ROTATION_RATE_Z,dataArray[5]);

            contentValues.put(GRAVITY_X,dataArray[6]);
            contentValues.put(GRAVITY_Y,dataArray[7]);
            contentValues.put(GRAVITY_Z,dataArray[8]);

            contentValues.put(ATTITUDE_AZIMUTH,dataArray[9]);
            contentValues.put(ATTITUDE_PITCH,dataArray[10]);
            contentValues.put(ATTITUDE_ROLL,dataArray[11]);

            long result = db.insert(TABLE_NAME,null ,contentValues);
            if(result == -1)
                return false;
            else
                continue;
        }
        return true;
    }

    //Deletes all values from a table
    public void deleteAllValues(){
        db.execSQL("delete from "+ TABLE_NAME);
    }

    //Returns all values from a table
    public Cursor viewData(){
        String query = "select * from "+TABLE_NAME;
        Cursor cursor = db.rawQuery(query,null);
        return cursor;
    }

    //Return single column values for given id
    public Cursor viewSingleData (Integer id){
        String query = "select * from "+TABLE_NAME+ " where "+DATA_INDEX+" = "+id+"";
        Cursor cursor = db.rawQuery(query,null);
        return cursor;
    }

    //Updates location, latitude, longitude column of the given id
    /*public boolean updateData(Integer id, String latitude, String longitude) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EMPLOYEE_LOCATION,1);
        contentValues.put(EMPLOYEE_LATITUDE,latitude);
        contentValues.put(EMPLOYEE_LONGITUDE,longitude);
        db.update(TABLE_NAME, contentValues, EMPLOYEE_ID+"="+id, null);
        return true;
    }*/
}
