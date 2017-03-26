package com.example.cafeclimber.hackrfsignalviewer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mantz_it.hackrf_android.Hackrf;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    // Android related Objects
    private Bundle savedInstanceState = null;
    private LocationManager locationManager = null;
    private Location location = null;

    // GUI Elements
    private TextView tv_rssi = null;
    private TextView tv_lat  = null;
    private TextView tv_long = null;
    private TextView tv_log  = null;
    private Button   bt_start = null;
    private Button   bt_stop = null;

    private HackRFSource hackRFSource = null;
    private ProcessingLoop processingLoop = null;

    private static final int MY_PERMISSIONS_ACCESS_LOCATION = 0;

    public enum LogLevel {
        INFO, WARNING, ERROR,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.savedInstanceState = savedInstanceState;

        String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String defaultFile = "HackRF/hackrf_android.iq";

        tv_rssi = (TextView) findViewById(R.id.tv_rssi);
        tv_lat  = (TextView) findViewById(R.id.tv_lat);
        tv_long = (TextView) findViewById(R.id.tv_long);
        tv_log  = (TextView) findViewById(R.id.tv_log);
        tv_log.setMovementMethod(new ScrollingMovementMethod());

        bt_start = (Button) findViewById(R.id.bt_start);
        bt_stop = (Button) findViewById(R.id.bt_stop);

        hackRFSource = new HackRFSource();
    }

    @Override
    protected void onStart() {
        super.onStart();

        bt_start.setEnabled(true);
        bt_stop.setEnabled(false);
    }

    public void start_recording(View v) {
        if (hackRFSource.open(this)) {
            bt_start.setEnabled(false);
            bt_stop.setEnabled(true);

            // Initialize location (If there's no HackRF Device what's the point in getting permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_LOCATION);
            }
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                tv_lat.setText(String.valueOf(location.getLatitude()));
                tv_long.setText(String.valueOf(location.getLongitude()));
            }

        }
        else {
            Toast.makeText(getApplicationContext(),
                    "No HackRF Device Available",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void stop_recording(View v) {
        hackRFSource.stopSampling();

        bt_start.setEnabled(true);
        bt_stop.setEnabled(false);
    }

    public void log_to_screen(LogLevel logLevel, String message) {
        switch (logLevel) {
            case INFO:
                tv_log.append("   [INFO] ");
                break;
            case WARNING:
                tv_log.append("[WARNING] ");
                break;
            case ERROR:
                tv_log.append("  [ERROR] ");
                break;
        }
        tv_log.append(message + "\n");
    }
}

