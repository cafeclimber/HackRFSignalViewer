package com.example.cafeclimber.hackrfsignalviewer;

/**
 * Created by cafeclimber on 3/26/17.
 */

public class Signed8bitIQConverter extends IQConverter {

    public Signed8bitIQConverter() {
        super();
    }

    @Override
    protected void generateLookupTable() {
        lookupTable = new float[256];
        for (int i = 0; i < 256; i++) {
            lookupTable[i] = (i - 128) / 128.0f;
        }
    }

    @Override
    protected void generateMixerLookupTable(int mixFrequency) {
        if (mixFrequency == 0 || (sampleRate / Math.abs(mixFrequency) > MAX_COSINE_LENGTH)) {
            mixFrequency += sampleRate;
        }

        if (cosineRealLookupTable == null || mixFrequency != cosineFrequency) {
            cosineFrequency = mixFrequency;
            int bestLength = calcOptimalCosineLength();
            cosineRealLookupTable = new float[bestLength][256];
            cosineImagLookupTable = new float[bestLength][256];
            float cosineAtT;
            float sineAtT;
            for (int t = 0; t < bestLength; t++) {
                cosineAtT = (float)Math.cos(2 * Math.PI * cosineFrequency * t / (float)sampleRate);
                sineAtT = (float)Math.sin(2 * Math.PI * cosineFrequency * t / (float)sampleRate);
                for (int i = 0; i < 256; i++) {
                    cosineRealLookupTable[t][i] = (i - 128) / 128.0f * cosineAtT;
                    cosineImagLookupTable[t][i] = (i - 128) / 128.0f * sineAtT;
                }
            }
            cosineIndex = 0;
        }
    }

    @Override
    public int fillPacketIntoSamplePacket(byte[] packet, SamplePacket samplePacket) {
        int capacity = samplePacket.capacity();
        int count = 0;
        int startIndex = samplePacket.size();
        float[] re = samplePacket.getRe();
        float[] im = samplePacket.getIm();
        for (int i = 0; i < packet.length; i += 2) {
            re[startIndex + count] = lookupTable[packet[i] + 128];
            im[startIndex + count] = lookupTable[packet[i + 1] + 128];
            count++;
            if (startIndex + count >= capacity) {
                break;
            }
        }
        samplePacket.setSize(samplePacket.size() + count);
        samplePacket.setSampleRate(sampleRate);
        samplePacket.setFrequency(frequency);
        return count;
    }

    @Override
    public int mixPacketIntoSamplePacket(byte[] packet,
                                         SamplePacket samplePacket,
                                         long channelFrequency) {
        int mixFrequency = (int)(frequency - channelFrequency);

        generateMixerLookupTable(mixFrequency);

        int capacity = samplePacket.capacity();
        int count = 0;
        int startIndex = samplePacket.size();
        float[] re = samplePacket.getRe();
        float[] im = samplePacket.getIm();
        for (int i = 0; i < packet.length; i += 2) {
            re[startIndex + count] = cosineRealLookupTable[cosineIndex][packet[i] + 128] -
                    cosineImagLookupTable[cosineIndex][packet[i + 1] + 128];
            im[startIndex + count] = cosineRealLookupTable[cosineIndex][packet[i + 1] + 128] -
                    cosineImagLookupTable[cosineIndex][packet[i] + 128];
            cosineIndex = (cosineIndex + 1) % cosineRealLookupTable.length;
            count++;
            if (startIndex + count >= capacity) {
                break;
            }
        }
        samplePacket.setSize(samplePacket.size() + count);
        samplePacket.setSampleRate(sampleRate);
        samplePacket.setFrequency(channelFrequency);
        return count;
    }
}
