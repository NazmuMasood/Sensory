package com.example.sensory;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Notification;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.sensory.DatabaseHelper.DATABASE_NAME;

public class MainActivity extends AppCompatActivity implements
        SensorEventListener, AdapterView.OnItemSelectedListener {

    Button startButton, stopButton, deleteDbButton, createDbButton;
    TextView accInfoTV, gyroInfoTV, gravityInfoTV, orientationInfoTV,
            accUncalibInfoTV, gyroUncalibInfoTV, timerTV;
    Boolean record = false; DatabaseHelper myDb;
    Boolean hasAccelerometer=false, hasGyroscope=false,
            hasGravity=false, hasMagnetometer=false;
    SensorManager sensorManager;
    Sensor accelerometer, gyroscope, gravity, magnetometer,
            accelerometerUncalib, gyroscopeUncalib;
    private static final int PERMISSION_REQUEST_CODE = 1; Boolean permissionGranted = false;

    //Timer implementation
    long startTime, timeTakenInSeconds; int samplingPeriod;
    public int seconds = 5;
    public int minutes = 0;
    Button refreshButton; Boolean refreshNeeded = false;

    //Activity drop-down list implementation
    Spinner activitySpinner;
    String[] activities = new String[]{"Walking", "Running",
            "Walking_upstairs", "Walking_downstairs"
            , "Sitting", "Exercise", "Pushup", "Laying",
            "Falling_down", "Jumping"
    };


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPermission())
        {
            // Code for above or equal 23 API Oriented Device
            // Your Permission granted already .Do next code
            permissionGranted = true;
        } else {
            requestPermission(); // Code for permission
        }

        //Setting up the activity list
        activitySpinner = findViewById(R.id.activitySpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, activities);
        activitySpinner.setAdapter(adapter);
        activitySpinner.setOnItemSelectedListener(this);

        //Declare the timers
        final Timer T = new Timer();
        final Timer t = new Timer();
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionGranted) {
                    if (refreshNeeded){
                        Toast.makeText(MainActivity.this, "Please refresh the timer first",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        //Create the file and append column headers
                        String baseDir = Environment.getExternalStorageDirectory().getPath();
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String selectedActivity = activitySpinner.getSelectedItem().toString();
                        String dataFileName = selectedActivity+"_" + timeStamp + ".csv";
                        filePath = baseDir+File.separator+ dataFileName;
                        writer = new CSVWriter(new FileWriter(filePath));
                        String[] csvHeader = ("timeInMilisecond#timestamp#accX#accY#accZ"
                                + "#gyroX#gyroY#gyroZ"
                                //    + "#heading7#heading8#heading9"
                                //    + "#heading10#heading11#heading12"
                        ).split("#");
                        writer.writeNext(csvHeader);
                        writer.close();
                    }
                    catch (Exception e){e.printStackTrace();}

                    //We initially use a timer to show 5 seconds time decreasingly
                    T.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    timerTV.setText(String.valueOf(minutes)+":"+String.valueOf(seconds));
                                    seconds -= 1;
                                    if(seconds == 0)
                                    {
                                        minutes=0;
                                        timerTV.setText(String.valueOf(minutes)+":"+String.valueOf(seconds));
                                    }
                                }

                            });
                        }

                    }, 0, 1000);

                    //This handler is there to make sure we collect..
                    // ..data after 5 seconds of pressing "start" button
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            record = true;
                            if (T != null){ T.cancel(); }
                            seconds=0;minutes=0;
                            refreshNeeded = true;

                            //Set the schedule function and rate i.e. this is the timer stopwatch
                            t.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            timerTV.setText(/*String.valueOf(minutes)+":"+*/String.valueOf(seconds));
                                            seconds += 1;
                                            //change
                                            if(seconds == 0)
                                            {
                                                timerTV.setText(/*String.valueOf(minutes)+":"+*/String.valueOf(seconds));

                                                seconds=0;
                                               // minutes=minutes+1;
                                            }
                                            else if(seconds==31){
                                                record = false;

                                                if (t != null){
                                                    t.cancel();
                                                }
                                                refreshButton.setVisibility(View.VISIBLE);
                                                long endTime = System.currentTimeMillis();
                                                timeTakenInSeconds = (endTime - startTime) / 1000;
                                                makeEndScream();



                                            }
                                        }

                                    });
                                }

                            }, 0, 1000);

                            startTime = System.currentTimeMillis();
                            makeStartScream();
                        }
                    }, 5000);



                    /*for (int i=0; i<1; i++){
                        Float[] dataArray = new Float[12];
                        float count = 0.10f;
                        for (int j=0; j<dataArray.length; j++) {
                            dataArray[j] = 0.10f+count;
                            count+=0.10;
                        }
                        myDb.writeDataToDb(dataArray);
                    }*/
                    //handleTempCSV();


                }
                else {Toast.makeText(MainActivity.this, "Please grant storage permission first",
                        Toast.LENGTH_SHORT)
                        .show();}
            }
        });
        //stop button
        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record = false;

                if (t != null){
                    t.cancel();
                }
                refreshButton.setVisibility(View.VISIBLE);
                long endTime = System.currentTimeMillis();
                timeTakenInSeconds = (endTime - startTime) / 1000;
                makeEndScream();
            }
        });
        /*
        deleteDbButton = findViewById(R.id.deleteDbButton);
        deleteDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //myDb.deleteTable();
                MainActivity.this.deleteDatabase(Environment.getExternalStorageDirectory()
                        + File.separator + "/DataBase/" + File.separator
                        + DATABASE_NAME);
                Toast.makeText(MainActivity.this, "Database deleted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
        createDbButton = findViewById(R.id.createDbButton);
        createDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionGranted) {
                    myDb = new DatabaseHelper(MainActivity.this);
                    Toast.makeText(MainActivity.this, "Database created",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                else {Toast.makeText(MainActivity.this, "Please grant storage permission first",
                        Toast.LENGTH_SHORT)
                        .show();}
            }
        });*/

        accInfoTV = findViewById(R.id.accInfoTV);
        gyroInfoTV = findViewById(R.id.gyroInfoTV);
        gravityInfoTV = findViewById(R.id.gravityInfoTV);
        orientationInfoTV = findViewById(R.id.orientaionInfoTV);
        accUncalibInfoTV = findViewById(R.id.accUncalibInfoTV);
        gyroUncalibInfoTV = findViewById(R.id.gyroUncalibInfoTV);
        timerTV = findViewById(R.id.timerTV);
        refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });
        //myDb = new DatabaseHelper(this);

        //Create sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Get all sensor names on the phone
        String sensorNames ="Sensors :";
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensorList){
            sensorNames = sensorNames+"\n"+s.getName();
        }
        accInfoTV.setText(sensorNames+"\n\nNo accelerometer data available");

        //Accelerometer sensor
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
            hasAccelerometer = true;
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        //Gyroscope sensor
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)) {
            hasGyroscope = true;
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        //Gravity sensor
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            hasGravity = true;
            gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }
        //Magnetometer sensor
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            hasMagnetometer = true;
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        /*//Accelerometer_uncalibrated sensor
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED) != null) {
            accelerometerUncalib = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);
        }
        //Gravity_uncalibrated sensor
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED) != null) {
            gyroscopeUncalib = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        }*/

        samplingPeriod = 20000;//in microseconds
        //Registering sensor listeners
        sensorManager.registerListener(this, accelerometer, samplingPeriod);
        sensorManager.registerListener(this, gyroscope, samplingPeriod);
        /*sensorManager.registerListener(this, gravity, samplingPeriod);
        sensorManager.registerListener(this, magnetometer, samplingPeriod);*/
        /*sensorManager.registerListener(this, accelerometerUncalib, samplingPeriod);
        sensorManager.registerListener(this, gyroscopeUncalib, samplingPeriod);*/


    }



    CSVWriter writer; String filePath;
    private void writeToCSVnew(Float[] dataArray) {
        File f = new File(filePath);
        Log.d("writeToCSVNew", String.format("dataArray: acc= %f,%f,%f, gyro= %f,%f,%f",
                dataArray[0],dataArray[1],dataArray[2],
                dataArray[3],dataArray[4],dataArray[5]));
        ArrayList<String[]> dataArrayString= new ArrayList<>();
        try {
            // File exist
            if (f.exists() && !f.isDirectory()) {
                FileWriter mFileWriter = new FileWriter(filePath, true);
                writer = new CSVWriter(mFileWriter);

                String s1 = dataArray[0].toString();
                String s2 = dataArray[1].toString();
                String s3 = dataArray[2].toString();

                String s4 = "NA";
                String s5 = "NA";
                String s6 = "NA";
                if (hasGyroscope) {
                    s4 = dataArray[3].toString();
                    s5 = dataArray[4].toString();
                    s6 = dataArray[5].toString();
                }

//                String s7 = dataArray[6].toString();
//                String s8 = dataArray[7].toString();
//                String s9 = dataArray[8].toString();
//
//                String s10 = dataArray[9].toString();
//                String s11 = dataArray[10].toString();
//                String s12 = dataArray[11].toString();

                //Getting current timestamp
                Date date= new Date();
                long timeInMiliseconds = date.getTime();
                Timestamp timestamp = new Timestamp(timeInMiliseconds);
                String tInMils = "#"+timeInMiliseconds;
                String ts = "#"+timestamp;

                dataArrayString.add(new String[]{tInMils,ts,
                        s1, s2, s3,
                        s4, s5, s6,
                        //s7, s8, s9,
                        //s10, s11, s12
                });
                //System.out.println("Current : "+tInMils+" "+ts+" "+s1+" "+s2+" "+s3+" "+s4+" "+s5+" "+s6 );

                writer.writeAll(dataArrayString);
                writer.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    Float[] dataArray = new Float[6];
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (record) {

//            float[] accelerometerReading = new float[3];
//            float[] magnetometerReading = new float[3];
//            float[] rotationMatrix = new float[9];
//            float[] orientationAngles = new float[3];

            //If the sensor is accelerometer
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                String info = "";
                Float userAccX, userAccY, userAccZ;

                userAccX = event.values[0];
                userAccY = event.values[1];
                userAccZ = event.values[2];

                userAccX = Math.round(userAccX*1000000f)/1000000f;
                userAccY = Math.round(userAccY*1000000f)/1000000f;
                userAccZ = Math.round(userAccZ*1000000f)/1000000f;

                info = info + "userAcceleration.X " + userAccX + "\n" +
                        "userAcceleration.Y " + userAccY + "\n" +
                        "userAcceleration.Z " + userAccZ + "\n";

                accInfoTV.setText(info);

                dataArray[0]= userAccX;
                dataArray[1]= userAccY;
                dataArray[2]= userAccZ;

                //System.arraycopy(event.values, 0, accelerometerReading,
                // 0, accelerometerReading.length);

                //Important for roll pitch azimuth part
                //accelerometerReading = event.values;

            }

            //If the sensor is gyroscope
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                String info = "";
                Float rotationRateX, rotationRateY, rotationRateZ;

                rotationRateX = event.values[0];
                rotationRateY = event.values[1];
                rotationRateZ = event.values[2];

                rotationRateX = Math.round(rotationRateX*1000000f)/1000000f;
                rotationRateY = Math.round(rotationRateY*1000000f)/1000000f;
                rotationRateZ = Math.round(rotationRateZ*1000000f)/1000000f;

                info = "rotationRate.X " + rotationRateX + "\n" +
                        "rotationRate.Y " + rotationRateY + "\n" +
                        "rotationRate.Z " + rotationRateZ + "\n";

                gyroInfoTV.setText(info);

                dataArray[3]= rotationRateX;
                dataArray[4]= rotationRateY;
                dataArray[5]= rotationRateZ;
            }
/*
           //If the sensor is gravity
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                String info = "";
                Float gravityX, gravityY, gravityZ;
                gravityX = event.values[0];
                gravityY = event.values[1];
                gravityZ = event.values[2];
                gravityX = Math.round(gravityX*1000000f)/1000000f;
                gravityY = Math.round(gravityY*1000000f)/1000000f;
                gravityZ = Math.round(gravityZ*1000000f)/1000000f;
                info = "gravity.X " + gravityX + "\n" +
                        "gravity.Y " + gravityY + "\n" +
                        "gravity.Z " + gravityZ + "\n\n";
                gravityInfoTV.setText(info);
                dataArray[6]= gravityX;
                dataArray[7]= gravityY;
                dataArray[8]= gravityZ;
            }
            //If the sensor is magnetometer
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
               // System.arraycopy(event.values, 0, magnetometerReading,
                     //   0, magnetometerReading.length);
                magnetometerReading = event.values;
            }
            if (hasMagnetometer && hasAccelerometer){
                SensorManager.getRotationMatrix(rotationMatrix, null,
                        accelerometerReading, magnetometerReading);
                SensorManager.getOrientation(rotationMatrix, orientationAngles);
                Float azimuth = orientationAngles[0];
                Float pitch = orientationAngles[1];
                Float roll = orientationAngles[2];
                //azimuth = Math.round(azimuth*1000000f)/1000000f;
                //pitch = Math.round(pitch*1000000f)/1000000f;
                //roll = Math.round(roll*1000000f)/1000000f;
                String info = "";
                info = "attitude.Azimuth " + azimuth + "\n" +
                        "attitude.Pitch " + pitch + "\n" +
                        "attitude.Roll " + roll + "\n\n";
                orientationInfoTV.setText(info);
                dataArray[9]= azimuth;
                dataArray[10]= pitch;
                dataArray[11]= roll;
            }
            //If the sensor is accelerometer_uncalibrated
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER_UNCALIBRATED) {
                String info = "";
                Float userAccX, userAccY, userAccZ;
                userAccX = event.values[0];
                userAccY = event.values[1];
                userAccZ = event.values[2];
                info = info + "userAccelerationUncalibrated.X " + userAccX + "\n" +
                        "userAccelerationUncalibrated.Y " + userAccY + "\n" +
                        "userAccelerationUncalibrated.Z " + userAccZ + "\n\n";
                accUncalibInfoTV.setText(info);
            }
            //If the sensor is gyroscope
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
                String info = "";
                Float rotationRateX, rotationRateY, rotationRateZ;
                rotationRateX = event.values[0];
                rotationRateY = event.values[1];
                rotationRateZ = event.values[2];
                info = "\nrotationRateUncalibrated.X " + rotationRateX + "\n" +
                        "rotationRateUncalibrated.Y " + rotationRateY + "\n" +
                        "rotationRateUncalibrated.Z " + rotationRateZ + "\n\n";
                gyroUncalibInfoTV.setText(info);
            }*/

            //Write the event values into database
            //myDb.writeDataToDb(dataArray);
            //writeToCSV(dataArray);
            if (hasGyroscope) {
                if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    writeToCSVnew(dataArray);
                }
            }
            else {writeToCSVnew(dataArray);}
        }
    }

    private void makeStartScream() {
        Toast.makeText(this, "Data collection started.. \nSensor delay is "+samplingPeriod+" microseconds",
                Toast.LENGTH_LONG).show();
    }
    private void makeEndScream() {
        Toast.makeText(this, "Data collection ended.. \nData saved for "+timeTakenInSeconds+" seconds",
                Toast.LENGTH_LONG).show();
        //Append footer
        /*File f = new File(filePath);
        try {
            // File exist
            if (f.exists() && !f.isDirectory()) {
                FileWriter mFileWriter = new FileWriter(filePath, true);
                writer = new CSVWriter(mFileWriter);
                String[] csvFooter = ("Duration : "+timeTakenInSeconds+"seconds#Delay : "+samplingPeriod+"seconds"
                ).split("#");
                writer.writeNext(csvFooter);
                writer.close();
            }
        }catch (Exception e){e.printStackTrace();}*/
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }
}