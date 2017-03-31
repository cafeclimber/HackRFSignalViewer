package com.example.cafeclimber.hackrfsignalviewer;

public class SamplePacket {
    private float[] re;
    private float[] im;
    private long frequency;
    private int sampleRate;
    private int size;

    public SamplePacket(float[] re, float[] im, long frequency, int sampleRate) {
        this(re, im, frequency, sampleRate, re.length);
    }

    public SamplePacket(float[] re, float[] im, long frequency, int sampleRate, int size) {
        if (re.length != im.length)
            throw new IllegalArgumentException("Arrays must be of the same length");
        if (size > re.length)
            throw new IllegalArgumentException("Size must be smaller or equal to the array length");
        this.re = re;
        this.im = im;
        this.frequency = frequency;
        this.sampleRate = sampleRate;
        this.size = size;
    }

    public SamplePacket(int size) {
        this.re = new float[size];
        this.im = new float[size];
        this.frequency = 0;
        this.sampleRate = 0;
        this.size = 0;
    }

    public float[] re() {
        return re;
    }

    public float re(int i ) {
        return re[i];
    }

    public float[] im() {
        return im;
    }

    public float im(int i) {
        return im[i];
    }

    public int capacity() {
        return re.length;
    }

    public int size() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.min(size, re.length);
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
}
