package com.siq.rfidpoc;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import com.google.gson.Gson;
import com.impinj.octane.Tag;

import java.sql.Timestamp;
import java.util.Scanner;

public class AwsIotSimulator {
    private static final String clientEndpoint = "a2454bnq205u0a.iot.us-east-2.amazonaws.com";       // replace <prefix> and <region> with your own
    private static final String clientId = "bfcfe360442def4c701e4f001f6634c45d27a6748e605ead9bf90d56df0511bb";                              // replace with your own client ID. Use unique client IDs for concurrent connections.
    private static final String certificateFile = "bfcfe36044-certificate.pem.crt";                       // X.509 based certificate file
    private static final String privateKeyFile = "bfcfe36044-private.pem.key";
    private static AWSIotMqttClient client;

    private static final String topic = "my/siqdb";
    private static String deviceId = "delledge0";

    private static String read1 = "{\"epc\":{\"data\":[57856,20808,20996,328,9008,9702]},\"antennaPortNumber\":1,\"channelInMhz\":0.0,\"firstSeenTime\":{\"utc\":0},\"lastSeenTime\":{\"utc\":1493448023585880},\"peakRssiInDbm\":0.0,\"tagSeenCount\":0,\"tid\":{\"data\":[]},\"rfDopplerFrequency\":0.0,\"phaseAngleInRadians\":0.0,\"crc\":0,\"pcBits\":0,\"gpsCoodinates\":{\"latitude\":0.0,\"longitude\":0.0},\"modelDetails\":{\"modelName\":\"Other\",\"userMemorySizeBits\":0,\"epcSizeBits\":0,\"supportsQt\":false},\"readOperationResults\":[],\"antennaPortNumberPresent\":true,\"channelInMhzPresent\":false,\"firstSeenTimePresent\":false,\"lastSeenTimePresent\":true,\"peakRssiInDbmPresent\":false,\"fastIdPresent\":false,\"rfPhaseAnglePresent\":false,\"seenCountPresent\":false,\"crcPresent\":false,\"pcBitsPresent\":false,\"rfDopplerFrequencyPresent\":false,\"gpsCoordinatesPresent\":false}";
    private static String read2 = "{\"epc\":{\"data\":[57856,20808,20996,328,9008,9712]},\"antennaPortNumber\":1,\"channelInMhz\":0.0,\"firstSeenTime\":{\"utc\":0},\"lastSeenTime\":{\"utc\":1493448023585880},\"peakRssiInDbm\":0.0,\"tagSeenCount\":0,\"tid\":{\"data\":[]},\"rfDopplerFrequency\":0.0,\"phaseAngleInRadians\":0.0,\"crc\":0,\"pcBits\":0,\"gpsCoodinates\":{\"latitude\":0.0,\"longitude\":0.0},\"modelDetails\":{\"modelName\":\"Other\",\"userMemorySizeBits\":0,\"epcSizeBits\":0,\"supportsQt\":false},\"readOperationResults\":[],\"antennaPortNumberPresent\":true,\"channelInMhzPresent\":false,\"firstSeenTimePresent\":false,\"lastSeenTimePresent\":true,\"peakRssiInDbmPresent\":false,\"fastIdPresent\":false,\"rfPhaseAnglePresent\":false,\"seenCountPresent\":false,\"crcPresent\":false,\"pcBitsPresent\":false,\"rfDopplerFrequencyPresent\":false,\"gpsCoordinatesPresent\":false}";

    private static void initClient() {
        AwsUtil.KeyStorePasswordPair pair = AwsUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);
        client = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
    }

    public static void main(String[] args) throws InterruptedException, AWSIotException, AWSIotTimeoutException {
        initClient();

        Scanner s = new Scanner(System.in);
        Gson g = new Gson();
        AWSIotQos qos = AWSIotQos.QOS0;

        client.connect();


        try {
            Tag t1 = g.fromJson(read2, Tag.class);
            TagReadWrapper w = new TagReadWrapper();
            w.tagId = t1.getEpc().toString();
            w.createdAt = System.currentTimeMillis();
            w.deviceId = deviceId;
            w.tag = t1;

            NonBlockingPublishListener message = new NonBlockingPublishListener(topic, qos, g.toJson(w));
            client.publish(message);
        } finally {
            client.disconnect();
        }
    }
}
