package com.example.sensory;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import static com.example.sensory.DatabaseHelper.DATABASE_NAME;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Button startButton, stopButton, deleteDbButton, createDbButton; TextView sensorInfoTV;
    TextView accInfoTV, gyroInfoTV, gravityInfoTV, orientationInfoTV,
            accUncalibInfoTV, gyroUncalibInfoTV;
    Boolean record = false; DatabaseHelper myDb;
    Boolean hasAccelerometer=false, hasGyroscope=false,
            hasGravity=false, hasMagnetometer=false;
    SensorManager sensorManager;
    Sensor accelerometer, gyroscope, gravity, magnetometer,
            accelerometerUncalib, gyroscopeUncalib;
    private static final int PERMISSION_REQUEST_CODE = 1; Boolean permissionGranted = false;

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

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionGranted) {
                    //record = true;
                    for (int i=0; i<1; i++){
                        Float[] dataArray = new Float[12];
                        float count = 0.10f;
                        for (int j=0; j<dataArray.length; j++) {
                            dataArray[j] = 0.10f+count;
                            count+=0.10;
                        }
                        myDb.writeDataToDb(dataArray);
                    }
                }
                else {Toast.makeText(MainActivity.this, "Please grant storage permission first",
                        Toast.LENGTH_SHORT)
                        .show();}
            }
        });
        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record = false;
            }
        });
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
        });

        accInfoTV = findViewById(R.id.accInfoTV);
        gyroInfoTV = findViewById(R.id.gyroInfoTV);
        gravityInfoTV = findViewById(R.id.gravityInfoTV);
        orientationInfoTV = findViewById(R.id.orientaionInfoTV);
        accUncalibInfoTV = findViewById(R.id.accUncalibInfoTV);
        gyroUncalibInfoTV = findViewById(R.id.gyroUncalibInfoTV);
        sensorInfoTV = findViewById(R.id.sensorInfo);
        //myDb = new DatabaseHelper(this);

        //See which sensors are available
        checkSensorAvailibility();

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
        if (hasAccelerometer) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        //Gyroscope sensor
        if (hasGyroscope) {
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        //Gravity sensor
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            hasGravity = true; checkSensorAvailibility();
            gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }
        //Magnetometer sensor
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            hasMagnetometer = true; checkSensorAvailibility();
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

        /*int samplingPeriod = 500000;
        //Registering sensor listeners
        sensorManager.registerListener(this, accelerometer, samplingPeriod);
        sensorManager.registerListener(this, gyroscope, samplingPeriod);
        sensorManager.registerListener(this, gravity, samplingPeriod);
        sensorManager.registerListener(this, magnetometer, samplingPeriod);*/
        /*sensorManager.registerListener(this, accelerometerUncalib, samplingPeriod);
        sensorManager.registerListener(this, gyroscopeUncalib, samplingPeriod);*/


    }

    private void checkSensorAvailibility(){
        String message = "Sensors available :";
        PackageManager packageManager = getPackageManager();

        //If Accelerometer sensor exists in the device
        hasAccelerometer = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        if (hasAccelerometer){message = message+" accelerometer";}

        //If Gyroscope sensor
        hasGyroscope = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        if (hasGyroscope){message = message+" gyroscope";}

        //If Gravity sensor
        if (hasGravity){message = message+" gravity";}

        //If Magnetometer sensor
        if (hasMagnetometer){message = message+" magnetometer";}

        sensorInfoTV.setText(message);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (record) {
            Float[] dataArray = new Float[12];

            float[] accelerometerReading = new float[3];
            float[] magnetometerReading = new float[3];
            float[] rotationMatrix = new float[9];
            float[] orientationAngles = new float[3];

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
                        "userAcceleration.Z " + userAccZ + "\n\n";

                accInfoTV.setText(info);

                dataArray[0]= userAccX;
                dataArray[1]= userAccY;
                dataArray[2]= userAccZ;

                //System.arraycopy(event.values, 0, accelerometerReading,
                       // 0, accelerometerReading.length);
                accelerometerReading = event.values;
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
                        "rotationRate.Z " + rotationRateZ + "\n\n";

                gyroInfoTV.setText(info);

                dataArray[3]= rotationRateX;
                dataArray[4]= rotationRateY;
                dataArray[5]= rotationRateZ;
            }

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

                /*azimuth = Math.round(azimuth*1000000f)/1000000f;
                pitch = Math.round(pitch*1000000f)/1000000f;
                roll = Math.round(roll*1000000f)/1000000f;*/

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
            }

            //Write the event values into database
            //myDb.writeDataToDb(dataArray);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
