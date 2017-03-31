package com.example.cafeclimber.hackrfsignalviewer;

import android.content.Context;
import android.util.Log;

import com.mantz_it.hackrf_android.Hackrf;
import com.mantz_it.hackrf_android.HackrfCallbackInterface;
import com.mantz_it.hackrf_android.HackrfUsbException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * <h1>HackRF Source Interface</h1>
 *
 * Module: HackRFInterface.java
 * Description: A class providing a means of interfacing with a HackRF
 *
 * @author Ryan Cmampbell
 *
 * Copyright (C) 2016 Ryan Campbell
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
 *
 */
public class HackRFInterface implements HackrfCallbackInterface{
    private Hackrf hackrf = null;
    private String name = null;
    private ArrayBlockingQueue<byte[]> queue = null;
    private IQConverter iqConverter = null;
    private long frequency = 0;
    private int sampleRate = 0;
    private int basebandFilterWidth = 0;
    private int vgaRxGain = 0;
    private int vgaTxGain = 0;
    private int lnaGain = 0;
    private boolean amplifier = false;
    private boolean antennaPower = false;

    public static final long MIN_FREQ = 1;
    public static final long MAX_FREQ = 7250000000l;
    public static final int MIN_SAMPLE_RATE = 4000000;
    public static final int MAX_SAMPLE_RATE = 20000000;
    public static final int MAX_VGA_RX_GAIN = 62;
    public static final int MAX_VGA_TX_GAIN = 47;
    public static final int MAX_LNA_GAIN = 40;
    public static final int VGA_RX_GAIN_STEP_SIZE = 2;
    public static final int VGA_TX_GAIN_STEP_SIZE = 1;
    public static final int LNA_GAIN_STEP_SIZE = 8;

    public static final String LOGTAG = "HackRFInterface";

    public HackRFInterface() {
        // TODO: Initialize hackRF on construction
        iqConverter = new IQConverter();
    }

    @Override
    public void onHackrfReady(Hackrf hackrf) {
        // TODO: Logging
        this.hackrf = hackrf;
    }

    @Override
    public void onHackrfError(String message) {
        // TODO: Logging
    }

    public boolean open(Context context) {
        int queueSize = 1000000;
        return Hackrf.initHackrf(context, this, queueSize);
    }

    public boolean isOpen() {
        if(hackrf == null)
            return false;
        try {
            hackrf.getBoardID();
            return true;
        } catch (HackrfUsbException e) {
            return false;
        }
    }

    public String getName() {
        if(name == null && hackrf != null) {
            try {
                name = Hackrf.convertBoardIdToString(hackrf.getBoardID());
            } catch (HackrfUsbException e) {
            }
        }
        if(name != null)
            return name;
        else
            return "HackRF";
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        if(hackrf != null) {
            try {
                hackrf.setFrequency(frequency);
            } catch (HackrfUsbException e) {
                Log.e(LOGTAG, "setFrequency: Error while setting frequency: " + e.getMessage());
            }
        }
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        if (hackrf != null) {
            try {
                hackrf.setSampleRate(sampleRate, 1);
                hackrf.setBasebandFilterBandwidth(basebandFilterWidth);
            } catch (HackrfUsbException e) {
                Log.e(LOGTAG, "setSampleRate: Error while setting sample rate: " + e.getMessage());
                return;
            }
        }

        this.flushQueue();
        Log.d(LOGTAG,"setSampleRate: setting sample rate to " + sampleRate);
        this.sampleRate = sampleRate;
    }

    public int getVgaRxGain() {
        return vgaRxGain;
    }

    public void setVgaRxGain(int gain) {
        if(gain > MAX_VGA_RX_GAIN) {
            Log.e(LOGTAG, "setVgaRxGain: Value (" + gain + ") too high. " +
                    "Maximum is: " + MAX_VGA_RX_GAIN);
            return;
        }

        if(hackrf != null) {
            try {
                hackrf.setRxVGAGain(vgaRxGain);
            } catch (HackrfUsbException e) {
                Log.e(LOGTAG, "setVgaRxGain: Error while setting vga gain: " + e.getMessage());
                return;
            }
        }
        this.vgaRxGain = gain;
    }

    public int getVgaTxGain() {
        return vgaTxGain;
    }

    public void setVgaTxGain(int gain) {
        if(gain > MAX_VGA_TX_GAIN) {
            Log.e(LOGTAG, "setVgaTxGain: Value (" + gain + ") too high. " +
                    "Maximum is: " + MAX_VGA_TX_GAIN);
            return;
        }

        if(hackrf != null) {
            try {
                hackrf.setTxVGAGain(vgaTxGain);
            } catch (HackrfUsbException e) {
                Log.e(LOGTAG, "setVgaTxGain: Error while setting vga gain: " + e.getMessage());
                return;
            }
        }
        this.vgaTxGain = gain;
    }

    public int getLnaGain() {
        return lnaGain;
    }

    public void setLnaGain(int gain) {
        if(gain > MAX_LNA_GAIN) {
            Log.e(LOGTAG, "setLnaGain: Value (" + gain + ") too high. " +
                    "Maximum is: " + MAX_LNA_GAIN);
            return;
        }

        if(hackrf != null) {
            try {
                hackrf.setRxLNAGain(lnaGain);
            } catch (HackrfUsbException e) {
                Log.e(LOGTAG, "setLnaGain: Error while setting lna gain: " + e.getMessage());
                return;
            }
        }
        this.lnaGain = gain;
    }

    public boolean getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(boolean amplifier) {
        if(hackrf != null) {
            try {
                hackrf.setAmp(amplifier);
            } catch (HackrfUsbException e) {
                Log.e(LOGTAG, "setAmplifier: Error while setting amplifier: " + e.getMessage());
                return;
            }
        }
        this.amplifier = amplifier;
    }

    public boolean getAntennaPower() {
        return antennaPower;
    }

    public void setAntennaPower(boolean antennaPower) {
        if(hackrf != null) {
            try {
                hackrf.setAntennaPower(antennaPower);
            } catch (HackrfUsbException e) {
                Log.e(LOGTAG, "setAntennaPower: Error while setting antenna power: " + e.getMessage());
                return;
            }
        }
        this.antennaPower = antennaPower;
    }

    public int getPacketSize() {
        if(hackrf != null)
            return hackrf.getPacketSize();
        else {
            Log.e(LOGTAG, "getPacketSize: Hackrf instance is null");
            return 0;
        }
    }

    public byte[] getPacket(int timeout) {
        if(queue != null && hackrf != null) {
            try {
                byte[] packet = queue.poll(timeout, TimeUnit.MILLISECONDS);
                if(packet == null && (hackrf.getTransceiverMode() !=
                        Hackrf.HACKRF_TRANSCEIVER_MODE_RECEIVE)) {
                    Log.e(LOGTAG, "getPacket: HackRF is not in receiving mode!");
                }
                return packet;
            } catch (InterruptedException e) {
                Log.e(LOGTAG, "getPacket: Interrupted while waiting on queue");
                return null;
            }
        }
        else {
            Log.e(LOGTAG, "getPacket: Queue is null");
            return null;
        }
    }

    public void returnPacket(byte[] buffer) {
        if(hackrf != null)
            hackrf.returnBufferToBufferPool(buffer);
        else {
            Log.e(LOGTAG, "returnPacket: Hackrf instance is null");
        }
    }

    public void startSampling() {
        if(hackrf != null) {
            try {
                hackrf.setSampleRate(sampleRate, 1);
                hackrf.setFrequency(frequency);
                hackrf.setBasebandFilterBandwidth(basebandFilterWidth);
                hackrf.setRxVGAGain(vgaRxGain);
                hackrf.setRxLNAGain(lnaGain);
                hackrf.setAmp(amplifier);
                hackrf.setAntennaPower(antennaPower);
                this.queue = hackrf.startRX();
                Log.i(LOGTAG, "startSampling: Started HackRF with: "
                        + "sampleRate=" + sampleRate
                        + " frequency=" + frequency
                        + " basebandFilterWidth=" + basebandFilterWidth
                        + " rxVgaGain=" + vgaRxGain
                        + " lnaGain=" + lnaGain
                        + " amplifier=" + amplifier
                        + " antennaPower=" + antennaPower);
            } catch (HackrfUsbException e) {
                Log.e(LOGTAG, "startSampling: Error while set up hackrf: " + e.getMessage());
            }
        } else {
            Log.e(LOGTAG, "startSampling: Hackrf instance is null");
        }
    }

    public void stopSampling() {
        if(hackrf != null) {
            try {
                hackrf.stop();
            } catch (HackrfUsbException e) {
                Log.e(LOGTAG, "stopSampling: Error while stopping hackrf: " + e.getMessage());
            }
        } else {
            Log.e(LOGTAG, "stopSampling: Hackrf instance is null");
        }
    }

    public void setBasebandFilterWidth(int basebandFilterWidth) {
        this.basebandFilterWidth = hackrf.computeBasebandFilterBandwidth(basebandFilterWidth);
        if (hackrf != null) {
            try {
                hackrf.setBasebandFilterBandwidth(this.basebandFilterWidth);
            } catch (HackrfUsbException e) {
                Log.d(LOGTAG, "setBasebandFilterWidth: Error while setting base band filter width:"
                + e.getMessage());
            }
        }
    }

    public int fillPacketIntoSamplePacket(byte[] packet, SamplePacket samplePacket) {
        return this.iqConverter.fillPacketIntoSamplePacket(packet, samplePacket);
    }

    public int mixPacketIntoSamplePakcket(byte[] packet,
                                          SamplePacket samplePacket,
                                          long channelFrequency) {
        return this.iqConverter.mixPacketIntoSamplePacket(packet, samplePacket, channelFrequency);
    }

    public void flushQueue() {
        byte[] buffer;
        if (hackrf == null || queue == null)
            return;
        for (int i = 0; i < queue.size(); i++) {
            buffer = queue.poll();
            if (buffer == null)
                return;
            hackrf.returnBufferToBufferPool(buffer);
        }
    }
}
