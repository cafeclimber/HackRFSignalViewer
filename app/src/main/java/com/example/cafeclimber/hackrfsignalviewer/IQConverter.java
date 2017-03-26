package com.example.cafeclimber.hackrfsignalviewer;

/**
 * Created by cafeclimber on 3/26/17.
 */

public abstract class IQConverter {
    protected long frequency = 0;
    protected int sampleRate = 0;
    protected float[] lookupTable = null;
    protected float[][] cosineRealLookupTable = null;
    protected float[][] cosineImagLookupTable = null;
    protected int cosineFrequency;
    protected int cosineIndex;
    protected static final int MAX_COSINE_LENGTH = 500;

    public IQConverter() {
        generateLookupTable();
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
        if (this.sampleRate != sampleRate) {
            this.sampleRate = sampleRate;
            generateMixerLookupTable(cosineFrequency);
        }
    }

    protected int calcOptimalCosineLength() {
        double cycleLength = sampleRate / Math.abs((double)cosineFrequency);
        int bestLength = (int)cycleLength;
        double bestLengthError = Math.abs(bestLength - cycleLength);
        for (int i = 0; i * cycleLength < MAX_COSINE_LENGTH; i++) {
            if (Math.abs(i * cycleLength - (int)(i * cycleLength)) < bestLengthError) {
                bestLength = (int)(i * cycleLength);
                bestLengthError = Math.abs(bestLength - (i * cycleLength));
            }
        }
        return bestLength;
    }

    protected abstract void generateLookupTable();

    protected abstract void generateMixerLookupTable(int mixFrequency);

    public abstract int fillPacketIntoSamplePacket(byte[] packet, SamplePacket samplePacket);

    public abstract int mixPacketIntoSamplePacket(byte[] packet,
                                                  SamplePacket samplePacket,
                                                  long channelFrequency);
}
