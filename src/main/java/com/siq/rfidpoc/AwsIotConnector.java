package com.siq.rfidpoc;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import com.google.gson.Gson;
import com.impinj.octane.Tag;

import java.util.Scanner;

public class AwsIotConnector {
    private static final String clientEndpoint = "a2454bnq205u0a.iot.us-east-2.amazonaws.com";       // replace <prefix> and <region> with your own
    private static final String clientId = "bfcfe360442def4c701e4f001f6634c45d27a6748e605ead9bf90d56df0511bb";                              // replace with your own client ID. Use unique client IDs for concurrent connections.
    private static final String certificateFile = "bfcfe36044-certificate.pem.crt";                       // X.509 based certificate file
    private static final String privateKeyFile = "bfcfe36044-private.pem.key";
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
