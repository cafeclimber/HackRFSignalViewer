package com.example.cafeclimber.hackrfsignalviewer;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

/**
 * Created by cafeclimber on 4/1/17.
 */

public class ProcessingLoop extends Thread {
    private int fftSize = 0;
    private int frameRate = 10;
    private double load = 0;
    private boolean stopRequested = true;
    private float[] mag = null;

    private static final String LOGTAG = "ProcessingLoop";
    private static final int MAX_FRAMERATE = 30;
    private static final double LOW_THRESHOLD = 0.65;
    private static final double HIGH_THRESHOLD = 0.85;

    private FFT fft = null;
    private ArrayBlockingQueue<SamplePacket> inputQueue = null;
    private ArrayBlockingQueue<SamplePacket> returnQueue = null;

    public ProcessingLoop(int fftSize,
                          ArrayBlockingQueue<SamplePacket> inputQueue,
                          ArrayBlockingQueue<SamplePacket> returnQueue) {
        int order = (int) (Math.log(fftSize) / Math.log(2));
        if (fftSize != (1 << order))
            throw new IllegalArgumentException("FFT size must be a power of 2");
        this.fftSize = fftSize;

        this.fft = new FFT(fftSize);
        this.mag = new float[fftSize];
        this.inputQueue = inputQueue;
        this.returnQueue = returnQueue;
    }

    public int getFftSize() {
        return fftSize;
    }

    @Override
    public void start() {
        this.stopRequested = false;
        super.start();
    }

    public void stopLoop() {
        this.stopRequested = true;
    }

    public boolean isRunning() {
        return !this.stopRequested;
    }

    @Override
    public void run() {
        Log.i(LOGTAG, "Processing loop started. (Thread: " + this.getName() + ")");
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
                    Log.d(LOGTAG, "run: Timeout while waiting on input data. skip.");
                    continue;
                }
            } catch (InterruptedException e) {
                Log.e(LOGTAG, "run: Interrupted while polling from input queue. stop.");
                this.stopLoop();
                break;
            }

            frequency = samples.getFrequency();
            sampleRate = samples.getSampleRate();

            this.doProcessing(samples);

            returnQueue.offer(samples);
            sleepTime = (1000 / frameRate) - (System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 0) {
                    load = (System.currentTimeMillis() - startTime) / (1000.0 / frameRate);
                    sleep(sleepTime);
                } else
                    load = 1;
            } catch (Exception e) {
                Log.e(LOGTAG, "Error while calling sleep()");
            }
        }
        this.stopRequested = true;
        Log.i(LOGTAG, "Processing loop stopped. (Thread: " + this.getName() + ")");
    }

    public void doProcessing(SamplePacket samples) {
        float[] re = samples.re();
        float[] im = samples.im();

        this.fft.applyWindow(re, im);
        this.fft.fft(re, im);

        float realPower;
        float imagPower;
        int size = samples.size();
        for (int i = 0; i < size; i++) {
            int targetIndex = (i + size / 2) % size;

            realPower = re[i] / fftSize;
            realPower = realPower * realPower;
            imagPower = im[i] / fftSize;
            imagPower = imagPower * imagPower;
            mag[targetIndex] = (float) (10 * Math.log10(Math.sqrt(realPower + imagPower)));
        }
    }
}
