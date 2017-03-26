package com.example.cafeclimber.hackrfsignalviewer;

import android.content.Context;
import android.widget.TextView;

import com.mantz_it.hackrf_android.Hackrf;
import com.mantz_it.hackrf_android.HackrfCallbackInterface;
import com.mantz_it.hackrf_android.HackrfUsbException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * <h1>RF Analyzer - HackRF source</h1>
 *
 * Module:      HackrfSource.java
 * Description: Source class representing a HackRF device.
 *
 * @author Ryan Campbell
 *
 * Copyright (C) 2017 Ryan Campbell
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
public class HackRFSource implements HackrfCallbackInterface {
    private Hackrf hackrf = null;
    private String name = null;
    private ArrayBlockingQueue<byte[]> queue = null;
    private long frequency = 0;
    private int sampleRate = 0;
    private int basebandFilterWidth = 0;
    private boolean automaticBBFilterCalculation = true;
    private int vgaRxGain = 0;
    private int vgaTxGain = 0;
    private int lnaGain = 0;
    private boolean amplifier = false;
    private boolean antennaPower = false;
    private int frequencyOffset = 0;	// virtually offset the frequency according to an external up/down-converter
    private IQConverter iqConverter = null;
    public static final long MIN_FREQUENCY = 1l;
    public static final long MAX_FREQUENCY = 7250000000l;
    public static final int MAX_SAMPLERATE = 20000000;
    public static final int MIN_SAMPLERATE = 4000000;
    public static final int MAX_VGA_RX_GAIN = 62;
    public static final int MAX_VGA_TX_GAIN = 47;
    public static final int MAX_LNA_GAIN = 40;
    public static final int VGA_RX_GAIN_STEP_SIZE = 2;
    public static final int VGA_TX_GAIN_STEP_SIZE = 1;
    public static final int LNA_GAIN_STEP_SIZE = 8;
    public static final int[] OPTIMAL_SAMPLE_RATES = { 4000000, 6000000, 8000000, 10000000, 12500000, 16000000, 20000000};

    public HackRFSource() {
        this.iqConverter = new Signed8bitIQConverter();
    }

    /**
     * Tries to open a hackrf device with a queue size of 100000
     * @param context
     * @return if call to initHackrf() was successful
     */
    public boolean open(Context context) {
        int queueSize = 1000000;
        return Hackrf.initHackrf(context, this, queueSize);
    }

    /**
     * Checks if HackRF is open. If null, it's not. If a usbexception occurs, it's not.
     *
     * @return if HackRF is open or not
     */
    public boolean isOpen() {
        if (hackrf == null) {
            return false;
        }
        try {
            hackrf.getBoardID();
            return true;
        }
        catch (HackrfUsbException e) {
            return false;
        }
    }

    @Override
    public void onHackrfReady(Hackrf hackrf) {
        this.hackrf = hackrf;
        // TODO: Log? Toast?
    }

    @Override
    public void onHackrfError(String message) {
        // TODO: Log?
    }

    public String getName() {
        if (name == null && hackrf != null) {
            try {
                name = Hackrf.convertBoardIdToString(hackrf.getBoardID());
            }
            catch (HackrfUsbException e) {}
        }
        if (name != null) {
            return name;
        }
        else return "HackRF";
    }

    public long getFrequency() {
        return frequency + frequencyOffset;
    }

    public void setFrequency(long frequency) {
        long actualFrequency = frequency - frequencyOffset;
        if (hackrf != null) {
            try {
                hackrf.setFrequency(actualFrequency);
            }
            catch (HackrfUsbException e) {
                // TODO: Log
                return;
            }
        }
        this.flushQueue();
        this.frequency = actualFrequency;
        // TODO: IQ Converter frequency
    }

    public long getMinFrequency() { return MIN_FREQUENCY + frequencyOffset; }

    public long getMaxFrequency() {
        return MAX_FREQUENCY + frequencyOffset;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        if (isAutomaticBBFilterCalculation()) {
            setBasebandFilterWidth((int)(sampleRate * 0.75));
        }

        if (hackrf != null) {
            try {
                hackrf.setSampleRate(sampleRate, 1);
                hackrf.setBasebandFilterBandwidth(basebandFilterWidth);
            }
            catch (HackrfUsbException e) {
                // TODO: Log
                return;
            }
        }

        this.flushQueue();
        this.sampleRate = sampleRate;
        // TODO: IQ Converter sampleRate
    }

    public int getMinSamplerate() {
        return MIN_SAMPLERATE;
    }

    public int getMaxSamplerate() {
        return MAX_SAMPLERATE;
    }

    public int getVgaTxGain() {
        return vgaTxGain;
    }

    public void setVgaTxGain(int vgaTxGain) {
        if (vgaTxGain > MAX_VGA_TX_GAIN) {
            // TODO: Log
            return;
        }
        if (hackrf != null) {
            try {
                hackrf.setTxVGAGain(vgaTxGain);
            }
            catch (HackrfUsbException e) {
                // TODO: Log
                return;
            }
        }
        this.vgaTxGain = vgaTxGain;
    }

    public int getVgaRxGain() {
        return vgaRxGain;
    }

    public void setVgaRxGain(int vgaRxGain) {
        if (vgaRxGain > MAX_VGA_RX_GAIN) {
            // TODO: Log
            return;
        }
        if (hackrf != null) {
            try {
                hackrf.setRxVGAGain(vgaRxGain);
            }
            catch (HackrfUsbException e) {
                // TODO: Log
                return;
            }
        }
        this.vgaRxGain = vgaRxGain;
    }

    public int getLnaGain() {
        return lnaGain;
    }

    public void setLnaGain(int lnaGain) {
        if (lnaGain > MAX_LNA_GAIN) {
            // TODO: Log
            return;
        }
        if (hackrf != null) {
            try {
                hackrf.setRxLNAGain(lnaGain);
            }
            catch (HackrfUsbException e) {
                // TODO: Log
                return;
            }
        }
        this.lnaGain = lnaGain;
    }

    public boolean isAmplifierOn() {
        return amplifier;
    }

    public void setAmplifier(boolean amplifier) {
        if (hackrf != null) {
            try {
                hackrf.setAmp(amplifier);
            }
            catch (HackrfUsbException e) {
                // TODO: Log
                return;
            }
        }
        this.amplifier = amplifier;
    }

    public boolean isAntennaPowerOn() {
        return antennaPower;
    }

    public  void setAntennaPower(boolean antennaPower) {
        if (hackrf != null) {
            try {
                hackrf.setAntennaPower(antennaPower);
            }
            catch (HackrfUsbException e) {
                // TODO: Log
                return;
            }
        }
        this.antennaPower = antennaPower;
    }

    public int getFrequencyOffset() {
        return frequencyOffset;
    }

    public void setFrequencyOffset(int frequencyOffset) {
        this.frequencyOffset = frequencyOffset;
        // TODO: IQ Converter set frequency offset
    }

    public int getBasebandFilterWidth() {
        return basebandFilterWidth;
    }

    public void setBasebandFilterWidth(int basebandFilterWidth) {
        if (hackrf != null) {
            this.basebandFilterWidth = hackrf.computeBasebandFilterBandwidth(basebandFilterWidth);
            // TODO: Log
            try {
                hackrf.setBasebandFilterBandwidth(this.basebandFilterWidth);
            } catch (HackrfUsbException e) {
                // TODO: Log
                return;
            }
        }
    }

    public boolean isAutomaticBBFilterCalculation() {
        return automaticBBFilterCalculation;
    }

    public void setAutomaticBBFilterCalculation(boolean automaticBBFilterCalculation) {
        this.automaticBBFilterCalculation = automaticBBFilterCalculation;
    }

    public int getPacketSize() {
        if (hackrf != null) {
            return hackrf.getPacketSize();
        }
        else {
            // TODO: Log
            return 0;
        }
    }

    public byte[] getPacket(int timeout) {
        if (queue != null && hackrf != null) {
            try {
                byte[] packet = queue.poll(timeout, TimeUnit.MILLISECONDS);
                if (packet == null &&
                        (hackrf.getTransceiverMode() != Hackrf.HACKRF_TRANSCEIVER_MODE_RECEIVE)) {
                    // TODO: Log
                }
                return packet;
            }
            catch (InterruptedException e) {
                // TODO: Log
                return null;
            }
        }
        else {
            // TODO: Log
            return null;
        }
    }

    public void returnPacket(byte[] buffer) {
        if (hackrf != null) {
            hackrf.returnBufferToBufferPool(buffer);
        }
        else {
            // TODO: Log
        }
    }

    public void startSampling() {
        if (hackrf != null) {
            try {
                hackrf.setSampleRate(sampleRate, 1);
                hackrf.setFrequency(frequency);
                hackrf.setBasebandFilterBandwidth(basebandFilterWidth);
                hackrf.setRxVGAGain(vgaRxGain);
                hackrf.setRxLNAGain(lnaGain);
                hackrf.setAmp(amplifier);
                hackrf.setAntennaPower(antennaPower);
                this.queue = hackrf.startRX();
                // TODO: Log
            }
            catch (HackrfUsbException e) {
                // TODO: Log
            }
        }
        else {
            // TODO: Log
        }
    }

    public void stopSampling() {
        if (hackrf != null) {
            try {
                hackrf.stop();
            }
            catch (HackrfUsbException e) {
                // TODO: Log
            }
        }
        else {
            // TODO: Log
        }
    }

    public int fillPacketIntoSamplePacket(byte[] packet, SamplePacket samplePacket) {
        return this.iqConverter.fillPacketIntoSamplePacket(packet, samplePacket);
    }

    public int mixPacketIntoSamplePacket(byte[] packet,
                                         SamplePacket samplePacket,
                                         long channelFrequency) {
        return this.iqConverter.mixPacketIntoSamplePacket(packet, samplePacket, channelFrequency);
    }

    public void flushQueue() {
        byte[] buffer;

        if (hackrf == null || queue == null) {
            return;
        }

        for (int i = 0; i < queue.size(); i++) {
            buffer = queue.poll();
            if (buffer == null) {
                return;
            }
            hackrf.returnBufferToBufferPool(buffer);
        }
    }
}
