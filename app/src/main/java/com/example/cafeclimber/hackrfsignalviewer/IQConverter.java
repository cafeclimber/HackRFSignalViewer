package com.example.cafeclimber.hackrfsignalviewer;

/**
 * Created by cafecilmber on 3/31/17.
 */

public class IQConverter {
    private long frequency = 0;
    private int sampleRate = 0;
    private float[] lookupTable = null;
    private float[][] cosineRealLookupTable = null;
    private float[][] cosineImagLookupTable = null;
    private int cosineFrequency;
    private int cosineIndex;

    private static final int MAX_COSINE_LENGTH = 500;

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
        return this.sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        if (this.sampleRate != sampleRate) {
            this.sampleRate = sampleRate;
            generateMixerLookupTable(cosineFrequency);
        }
    }

    private int calcOptimalCosineLength() {
        double cycleLength = sampleRate / Math.abs((double)cosineFrequency);
        int bestLength = (int) cycleLength;
        double bestLengthError = Math.abs(bestLength - cycleLength);
        for (int i = 0; i * cycleLength < MAX_COSINE_LENGTH; i++) {
            if (Math.abs(i * cycleLength) - (int)(i * cycleLength) < bestLengthError) {
                bestLength = (int)(i * cycleLength);
                bestLengthError = Math.abs(bestLength - (i * cycleLength));
            }
        }
        return bestLength;
    }

    public int fillPacketIntoSamplePacket(byte[] packet, SamplePacket samplePacket) {
        int capacity = samplePacket.capacity();
        int count = 0;
        int startIndex = samplePacket.size();
        float[] re = samplePacket.re();
        float[] im = samplePacket.im();
        for (int i = 0; i < packet.length; i += 2) {
            re[startIndex + count] = lookupTable[packet[i] + 128];
            im[startIndex + count] = lookupTable[packet[i + 1] + 128];
            count++;
            if (startIndex + count >= capacity)
                break;
        }
        samplePacket.setSize(samplePacket.size() + count);
        samplePacket.setSampleRate(sampleRate);
        samplePacket.setFrequency(frequency);
        return count;
    }

    public int mixPacketIntoSamplePacket(byte[] packet,
                                         SamplePacket samplePacket,
                                         long channelFrequency) {
        int mixFrequency = (int)(frequency - channelFrequency);
        generateLookupTable();
        int capacity = samplePacket.capacity();
        int count = 0;
        int startIndex = samplePacket.size();
        float[] re = samplePacket.re();
        float[] im = samplePacket.im();
        for (int i = 0; i < packet.length; i += 2) {
            re[startIndex + count] = cosineRealLookupTable[cosineIndex][packet[i] + 128] -
                    cosineImagLookupTable[cosineIndex][packet[i + 1] + 128];
            im[startIndex + count] = cosineRealLookupTable[cosineIndex][packet[i + 1] + 128] -
                    cosineImagLookupTable[cosineIndex][packet[i] + 128];
            cosineIndex = (cosineIndex + 1) % cosineRealLookupTable.length;
            count++;
            if (startIndex + count >= capacity)
                break;
        }
        samplePacket.setSize(samplePacket.size() + count);
        samplePacket.setSampleRate(sampleRate);
        samplePacket.setFrequency(channelFrequency);
        return count;
    }

    private void generateLookupTable() {
        lookupTable = new float[256];
        for (int i = 0; i < 256; i++)
            lookupTable[i] = (i - 128) / 128.0f;
    }

    private void generateMixerLookupTable(int mixFrequency) {
        if (mixFrequency == 0 || (sampleRate / Math.abs(mixFrequency) > MAX_COSINE_LENGTH))
            mixFrequency += sampleRate;

        if (cosineRealLookupTable == null || mixFrequency != cosineFrequency) {
            cosineFrequency = mixFrequency;
            int bestLength = calcOptimalCosineLength();
            cosineRealLookupTable = new float[bestLength][256];
            cosineImagLookupTable = new float[bestLength][256];
            float cosineAtT;
            float sineAtT;
            for (int t = 0; t < bestLength; t++) {
                cosineAtT = (float) Math.cos(2 * Math.PI * cosineFrequency * t / (float)sampleRate);
                sineAtT = (float) Math.sin(2 * Math.PI * cosineFrequency * t / (float)sampleRate);
                for (int i = 0; i < 256; i++) {
                    cosineRealLookupTable[t][i] = (i - 128) / 128.0f * cosineAtT;
                    cosineImagLookupTable[t][i] = (i - 128) / 128.0f * sineAtT;
                }
            }
            cosineIndex = 0;
        }
    }
}
