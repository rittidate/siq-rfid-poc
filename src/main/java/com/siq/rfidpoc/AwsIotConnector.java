package com.siq.rfidpoc;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import com.google.gson.Gson;
import com.impinj.octane.Tag;

import java.util.Scanner;

public class AwsIotConnector {
    private static final String clientEndpoint = "a2454bnq205u0a.iot.us-east-1.amazonaws.com";       // replace <prefix> and <region> with your own
    private static final String clientId = "05908b7e3707b8d7da94ae22d8ac5585d587ce22daaaefedafe498a555547ff9";                              // replace with your own client ID. Use unique client IDs for concurrent connections.
    private static final String certificateFile = "05908b7e37-certificate.pem.crt";                       // X.509 based certificate file
    private static final String privateKeyFile = "05908b7e37-private.pem.key";
    private static AWSIotMqttClient client;

    private static final String topic = "my/siqdb";
    private static String deviceId = "delledge0";

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
            while (true) {
                String msg = s.nextLine();
                Tag t1 = g.fromJson(msg, Tag.class);
                TagReadWrapper w = new TagReadWrapper();
                w.tagId = t1.getEpc().toString();
                w.createdAt = System.currentTimeMillis();
                w.deviceId = deviceId;
                w.tag = t1;

                NonBlockingPublishListener message = new NonBlockingPublishListener(topic, qos, g.toJson(w));
                client.publish(message);
            }
        } finally {
            client.disconnect();
        }
    }

}
