package com.example.sensory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    Button startButton, stopButton; TextView sensorInfoTV;
    TextView accInfoTV, gyroInfoTV, gravityInfoTV, orientationInfoTV;
    Boolean record = false;
    Boolean hasAccelerometer=false, hasGyroscope=false, hasGravity=false, hasMagnetometer=false;
    SensorManager sensorManager; Sensor accelerometer, gyroscope, gravity, magnetometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record = true;
            }
        });
        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record = false;
            }
        });
        accInfoTV = findViewById(R.id.accInfoTV);
        gyroInfoTV = findViewById(R.id.gyroInfoTV);
        gravityInfoTV = findViewById(R.id.gravityInfoTV);
        orientationInfoTV = findViewById(R.id.orientaionInfoTV);
        sensorInfoTV = findViewById(R.id.sensorInfo);

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
        //gyroInfoTV.setText("");gravityInfoTV.setText("");

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

        //Registering sensor listeners
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);

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

                info = info + "userAcceleration.X " + userAccX + "\n" +
                        "userAcceleration.Y " + userAccY + "\n" +
                        "userAcceleration.Z " + userAccZ + "\n\n";

                accInfoTV.setText(info);

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

                info = "rotationRate.X " + rotationRateX + "\n" +
                        "rotationRate.Y " + rotationRateY + "\n" +
                        "rotationRate.Z " + rotationRateZ + "\n\n";

                gyroInfoTV.setText(info);
            }

            //If the sensor is gravity
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                String info = "";
                Float gravityX, gravityY, gravityZ;

                gravityX = event.values[0];
                gravityY = event.values[1];
                gravityZ = event.values[2];

                info = "gravity.X " + gravityX + "\n" +
                        "gravity.Y " + gravityY + "\n" +
                        "gravity.Z " + gravityZ + "\n\n";

                gravityInfoTV.setText(info);
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

                String info = "";
                info = "attitude.Azimuth " + azimuth + "\n" +
                        "attitude.Pitch " + pitch + "\n" +
                        "attitude.Roll " + roll + "\n\n";

                orientationInfoTV.setText(info);
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
