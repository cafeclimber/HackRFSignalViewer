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
    private TextView tv_lat = null;
    private TextView tv_long = null;

    private int fftSize = 0;
    private int frameRate = 10;
    private boolean stopRequested = true;
    private float mag[] = null;

    private FFT fftBlock = null;

    private ArrayBlockingQueue<SamplePacket> inputQueue = null;
    private ArrayBlockingQueue<SamplePacket> returnQueue = null;

    public ProcessingLoop(View v,
                          int fftSize,
                          ArrayBlockingQueue<SamplePacket> inputQueue,
                          ArrayBlockingQueue<SamplePacket> returnQueue) {
        this.view = v;
        this.tv_rssi = (TextView) v.findViewById(R.id.tv_rssi);
        this.tv_lat = (TextView) v.findViewById(R.id.tv_lat);
        this.tv_long = (TextView) v.findViewById(R.id.tv_long);

        int order = (int) (Math.log(fftSize) / Math.log(2));
        if (fftSize != 1 << order) {
            throw new IllegalArgumentException("FFT size must be a power of 2");
        }
        this.fftSize = fftSize;
        this.mag = new float[fftSize];
        this.inputQueue = inputQueue;
        this.returnQueue= returnQueue;
    }

    @Override
    public void start() {
        this.stopRequested = false;
        super.start();
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

            this.doProcessing(samples);
            returnQueue.offer(samples);

            int start = (mag.length / 2) - 5;
            int end = (mag.length / 2) + 5;
            int rssi = 0;

            for (int i = start; i < end; i++) {
                rssi += mag[i];
            }

            rssi = rssi / (end - start);
            tv_rssi.setText(rssi);



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

    public void doProcessing(SamplePacket samples) {
        float[] re = samples.getRe();
        float[] im = samples.getIm();
        this.fftBlock.applyWindow(re, im);
        this.fftBlock.fft(re, im);

        float realPower;
        float imagPower;
        int size = samples.size();
        for (int i = 0; i < size; i++) {
            int targetIndex = (i + size / 2)  % size;

            realPower = re[i] / fftSize;
            realPower = realPower * realPower;
            imagPower = im[i] / fftSize;
            imagPower = imagPower * imagPower;
            mag[targetIndex] = (float) (10 * Math.log10(Math.sqrt(realPower + imagPower)));
        }
    }

    public boolean isRunning() {
        return !stopRequested;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getFftSize() {
        return fftSize;
    }

}
