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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private EditText et_frequency = null;
    private Button   bt_start = null;
    private Button   bt_stop = null;

    private boolean running = false;

    private static final int MY_PERMISSIONS_ACCESS_LOCATION = 0;

    public enum LogLevel {
        INFO, WARNING, ERROR,
    }

    /**
     * Gets references to GUI elements
     * @param savedInstanceState State to restore from
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.savedInstanceState = savedInstanceState;

        tv_rssi = (TextView) findViewById(R.id.tv_rssi);
        tv_lat  = (TextView) findViewById(R.id.tv_lat);
        tv_long = (TextView) findViewById(R.id.tv_long);
        tv_log  = (TextView) findViewById(R.id.tv_log);
        tv_log.setMovementMethod(new ScrollingMovementMethod());
        et_frequency = (EditText) findViewById(R.id.et_frequency);

        bt_start = (Button) findViewById(R.id.bt_start);
        bt_stop = (Button) findViewById(R.id.bt_stop);
    }

    /**
     * Checks for location permissions on start.
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_LOCATION);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Prints to onscreen log
     * @param logLevel Prepends message with appropriate Log level
     * @param message String to print to log
     */
    public void logToScreen(LogLevel logLevel, String message) {
        switch (logLevel) {
            case INFO:
                tv_log.append("         [INFO] ");
                break;
            case WARNING:
                tv_log.append("[WARNING] ");
                break;
            case ERROR:
                tv_log.append("     [ERROR] ");
                break;
        }
        tv_log.append(message + "\n");
    }

    public void setButtonsRunning() {
        bt_start.setEnabled(!running);
        bt_stop.setEnabled(running);
    }
}

