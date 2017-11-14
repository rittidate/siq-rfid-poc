package com.siq.rfidpoc;

import com.google.gson.Gson;
import com.impinj.octane.ImpinjTimestamp;
import com.impinj.octane.Tag;

import java.lang.reflect.Field;

public class ReaderSimulator {
    private static String read1 = "{\"epc\":{\"data\":[57856,20808,20996,328,9008,9702]},\"antennaPortNumber\":1,\"channelInMhz\":0.0,\"firstSeenTime\":{\"utc\":0},\"lastSeenTime\":{\"utc\":1493448023585880},\"peakRssiInDbm\":0.0,\"tagSeenCount\":0,\"tid\":{\"data\":[]},\"rfDopplerFrequency\":0.0,\"phaseAngleInRadians\":0.0,\"crc\":0,\"pcBits\":0,\"gpsCoodinates\":{\"latitude\":0.0,\"longitude\":0.0},\"modelDetails\":{\"modelName\":\"Other\",\"userMemorySizeBits\":0,\"epcSizeBits\":0,\"supportsQt\":false},\"readOperationResults\":[],\"antennaPortNumberPresent\":true,\"channelInMhzPresent\":false,\"firstSeenTimePresent\":false,\"lastSeenTimePresent\":true,\"peakRssiInDbmPresent\":false,\"fastIdPresent\":false,\"rfPhaseAnglePresent\":false,\"seenCountPresent\":false,\"crcPresent\":false,\"pcBitsPresent\":false,\"rfDopplerFrequencyPresent\":false,\"gpsCoordinatesPresent\":false}";
    private static String read2 = "{\"epc\":{\"data\":[57856,20808,20996,328,9008,9702]},\"antennaPortNumber\":1,\"channelInMhz\":0.0,\"firstSeenTime\":{\"utc\":0},\"lastSeenTime\":{\"utc\":1493448023585880},\"peakRssiInDbm\":0.0,\"tagSeenCount\":0,\"tid\":{\"data\":[]},\"rfDopplerFrequency\":0.0,\"phaseAngleInRadians\":0.0,\"crc\":0,\"pcBits\":0,\"gpsCoodinates\":{\"latitude\":0.0,\"longitude\":0.0},\"modelDetails\":{\"modelName\":\"Other\",\"userMemorySizeBits\":0,\"epcSizeBits\":0,\"supportsQt\":false},\"readOperationResults\":[],\"antennaPortNumberPresent\":true,\"channelInMhzPresent\":false,\"firstSeenTimePresent\":false,\"lastSeenTimePresent\":true,\"peakRssiInDbmPresent\":false,\"fastIdPresent\":false,\"rfPhaseAnglePresent\":false,\"seenCountPresent\":false,\"crcPresent\":false,\"pcBitsPresent\":false,\"rfDopplerFrequencyPresent\":false,\"gpsCoordinatesPresent\":false}";
    public static void main(String[] args) throws InterruptedException, NoSuchFieldException, IllegalAccessException{
        Gson g = new Gson();
        Tag t1 = g.fromJson(read1, Tag.class);
        Tag t2 = g.fromJson(read2, Tag.class);

        Field f = t1.getClass().getDeclaredField("lastSeenTime");
        f.setAccessible(true);

        while(true){

            Thread.sleep(50);
            long now = System.currentTimeMillis() * 1000;
            ImpinjTimestamp its = new ImpinjTimestamp(now);
            f.set(t1, its);

            System.out.println(g.toJson(t1));
        }

    }
}
