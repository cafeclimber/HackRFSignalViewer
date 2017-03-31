package com.example.cafeclimber.hackrfsignalviewer;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by cafecilmber on 3/31/17.
 */

public class Scheduler extends Thread {
    private HackRFInterface source= null;
    private ArrayBlockingQueue<SamplePacket> fftInputQueue = null;
    private ArrayBlockingQueue<SamplePacket> fftOutputQueue = null;
    private boolean stopRequested = true;

    private static final int FFT_QUEUE_SIZE = 2;
    private static final String LOGTAG = "Scheduler";

    public Scheduler(int fftSize, HackRFInterface source) {
        this.source = source;

        this.fftInputQueue = new ArrayBlockingQueue<SamplePacket>(FFT_QUEUE_SIZE);
        this.fftOutputQueue = new ArrayBlockingQueue<SamplePacket>(FFT_QUEUE_SIZE);
        for (int i = 0; i < FFT_QUEUE_SIZE; i++)
            fftInputQueue.offer(new SamplePacket(fftSize));
    }

    public void stopScheduler() {
        this.stopRequested = true;
        this.source.startSampling();
    }

    public void start() {
        this.stopRequested = false;
        this.source.startSampling();
        super.start();
    }

    public boolean isRunning() {
        return !stopRequested;
    }

    public ArrayBlockingQueue<SamplePacket> getFftInputQueue() {
        return fftInputQueue;
    }

    public ArrayBlockingQueue<SamplePacket> getFftOutputQueue() {
        return fftOutputQueue;
    }

    @Override
    public void run() {
        Log.i(LOGTAG, "Scheduler started. (Thread: " + this.getName() + ")");
        SamplePacket fftBuffer = null;

        while (!stopRequested) {
            byte[] packet = source.getPacket(1000); // TODO: Magic constant?
            if (packet == null) {
                Log.e(LOGTAG, "run: No more packets from source. Shutting down...");
                this.stopScheduler();
                break;
            }

            if (fftBuffer == null) {
                fftBuffer = fftInputQueue.poll();
                if (fftBuffer != null)
                    fftBuffer.setSize(0);
            }

            if (fftBuffer != null) {
                source.fillPacketIntoSamplePacket(packet, fftBuffer);

                if (fftBuffer.capacity() == fftBuffer.size()) {
                    fftOutputQueue.offer(fftBuffer);
                    fftBuffer = null;
                }
            }
            source.returnPacket(packet);
        }
        this.stopRequested = true;
        Log.i(LOGTAG, "Scheduler stopper. (Thread: " + this.getName() + ")");
    }
}
