package com.example.cafeclimber.hackrfsignalviewer;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by cafeclimber on 3/26/17.
 */

public class Scheduler extends Thread {
    private HackRFSource hackRFSource = null;
    private ArrayBlockingQueue<SamplePacket> fftOutputQueue = null;
    private ArrayBlockingQueue<SamplePacket> fftInputQueue = null;

}
