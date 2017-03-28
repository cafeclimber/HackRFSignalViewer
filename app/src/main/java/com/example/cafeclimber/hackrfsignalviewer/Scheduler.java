package com.example.cafeclimber.hackrfsignalviewer;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by cafeclimber on 3/26/17.
 */

public class Scheduler extends Thread {
    private HackRFSource hackRFSource = null;
    private ArrayBlockingQueue<SamplePacket> fftOutputQueue = null;
    private ArrayBlockingQueue<SamplePacket> fftInputQueue = null;
    private boolean stopRequested = true;

    private static final int FFT_QUEUE_SIZE = 2;

    public Scheduler(int fftSize, HackRFSource source) {
        this.hackRFSource = source;
        this.fftOutputQueue = new ArrayBlockingQueue<SamplePacket>(FFT_QUEUE_SIZE);
        this.fftInputQueue = new ArrayBlockingQueue<SamplePacket>(FFT_QUEUE_SIZE);
        for (int i = 0; i < FFT_QUEUE_SIZE; i++) {
            fftInputQueue.offer(new SamplePacket(fftSize));
        }
    }

    public void stopScheduler() {
        this.stopRequested = true;
        this.hackRFSource.stopSampling();
    }

    public void start() {
        this.stopRequested = false;
        this.hackRFSource.startSampling();
        super.start();
    }

    public boolean isRunning() {
        return !stopRequested;
    }

    public ArrayBlockingQueue<SamplePacket> getFftOutputQueue() {
        return fftOutputQueue;
    }

    public ArrayBlockingQueue<SamplePacket> getFftInputQueue() {
        return fftInputQueue;
    }

    @Override
    public void run() {
        // TODO: Log
        SamplePacket fftBuffer = null;

        while(!stopRequested) {
            byte[] packet = hackRFSource.getPacket(1000);
            if (packet == null) {
                // TODO: Log
                this.stopScheduler();
                break;
            }

            if (fftBuffer == null) {
                fftBuffer = fftInputQueue.poll();
                if (fftBuffer != null) {
                    fftBuffer.setSize(0);
                }
            }

            if (fftBuffer != null) {
                hackRFSource.fillPacketIntoSamplePacket(packet, fftBuffer);
                if (fftBuffer.capacity() == fftBuffer.size()) {
                    fftOutputQueue.offer(fftBuffer);
                    fftBuffer = null;
                }
            }

            hackRFSource.returnPacket(packet);
        }
        this.stopRequested = true;
        // TODO: Log
    }
}
