package com.example.cafeclimber.hackrfsignalviewer;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by cafeclimber on 3/26/17.
 */

public class ProcessingLoop extends Thread {
    private View view = null;
    private TextView tv_rssi = null;

    private boolean stopRequested = true;
    private int frameRate = 10;

    private ArrayBlockingQueue<SamplePacket> inputQueue = null;
    private ArrayBlockingQueue<SamplePacket> returnQueue = null;

    public ProcessingLoop(View v,
                          ArrayBlockingQueue<SamplePacket> inputQueue,
                          ArrayBlockingQueue<SamplePacket> returnQueue) {
        this.view = v;
        this.inputQueue = inputQueue;
        this.returnQueue= returnQueue;
    }

    private void calcRssi(SamplePacket samples) {
        float[] re = samples.getRe();
        float[] im = samples.getIm();
        float rssi = -99;

        float realPower;
        float imagPower;
        int size = samples.size();
        realPower = re[re.length / 2] * re[re.length / 2];
        imagPower = im[im.length / 2] * im[im.length / 2];

        rssi = (float) (10 * Math.log10(Math.sqrt(realPower + imagPower)));
        tv_rssi.setText(String.valueOf(rssi));

    }

    @Override
    public void start() {
        this.stopRequested = false;
        this.tv_rssi = (TextView) view.findViewById(R.id.tv_rssi);
        super.start();
    }

    public boolean isRunning() {
        return !stopRequested;
    }

    @Override
    public void run() {
        long startTime;
        long sleepTime;
        long frequency;
        int sampleRate;

        while (!stopRequested) {
            startTime = System.currentTimeMillis();
            SamplePacket samples;
            try {
                samples = inputQueue.poll(1000 / frameRate, TimeUnit.MILLISECONDS);
                if (samples == null) {
                    // TODO: Log
                    continue;
                }
            }
            catch (InterruptedException e) {
                // TODO: Log
                this.stopLoop();
                break;
            }

            frequency = samples.getFrequency();
            sampleRate = samples.getSampleRate();

            this.calcRssi(samples);
            returnQueue.offer(samples);

            sleepTime = (1000 / frameRate) - (System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 0) {
                    sleep(sleepTime);
                }
                else {
                    frameRate--;
                }
            }
            catch (InterruptedException e) {
                Log.e("ProcessingLoop", "Error while calling sleep()");
            }
        }
        this.stopRequested = true;
    }

    public void stopLoop() {
        this.stopRequested = true;
    }
}
