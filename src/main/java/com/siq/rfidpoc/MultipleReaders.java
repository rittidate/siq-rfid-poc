package com.siq.rfidpoc;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.google.gson.Gson;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;
import com.impinj.octane.Tag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class MultipleReaders {
    static ArrayList<ImpinjReader> readers = new ArrayList<ImpinjReader>();
    static ArrayList<ReaderWrapper> inputReaders;
    private static boolean addingReader = true;

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

    public static void main(String[] args) throws AWSIotException {

        inputReaders = addMultipleReader();

        for(Iterator<ReaderWrapper> i = inputReaders.iterator(); i.hasNext();) {
            ReaderWrapper item = i.next();
            ImpinjReader reader = new ImpinjReader();
            reader.setName(item.name);
            try {
                System.out.println("Attempting connection to " + item.name);
                reader.connect(item.ipAddress);

            } catch (OctaneSdkException ex) {
                // keep trying other readers if this doesn't work
                System.out.println("Error Connecting  to " + item.name + ": "
                    + ex.toString() + "...continuing with other readers");
                continue;
            }

            try {
                Settings settings = reader.queryDefaultSettings();
                System.out.println("Applying Settings to " + item.name);
                reader.applySettings(settings);

                reader.setTagReportListener(
                    new TagReportListenerImplementation());

                System.out.println("Starting " + item.name );
                reader.start();
                readers.add(reader);
            } catch (OctaneSdkException ex) {
                System.out.println("Could not start reader " + item.name  + ": "
                    + ex.toString());
            }
        }

        System.out.println("Press Enter to continue and read all tags.");
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
            //disconnect all reader
            for (int i = 0; i < readers.size(); i++) {

                try {
                    ImpinjReader reader = readers.get(i);
                    reader.stop();
                    reader.disconnect();
                } catch (OctaneSdkException ex) {
                    System.out.println("Failed to stop reader: " + ex.getMessage());
                }

            }
            client.disconnect();
        }


    }

    public static ArrayList<ReaderWrapper> addMultipleReader(){
        ArrayList<ReaderWrapper> readers = new ArrayList<ReaderWrapper>();

        while(addingReader){
            Scanner inputReader = new Scanner(System.in);
            System.out.println("Add reader hostname or IP address...");
            ReaderWrapper readerWrapper = new ReaderWrapper();
            readerWrapper.ipAddress = inputReader.nextLine();
            System.out.println("Add name reader...");
            String readerName = inputReader.nextLine();
            if(readerName.trim().equals("")){
                int numberReader = readers.size() + 1;
                readerWrapper.name = "Reader_" + numberReader;
            } else {
                readerWrapper.name = readerName;
            }
            readers.add(readerWrapper);

            System.out.println("Do you want to continue add reader? [Y/N]");
            String addMoreReader = inputReader.nextLine().toLowerCase();

            if(addMoreReader.equals("y") || addMoreReader.equals("yes")) {

            }else if(addMoreReader.equals("n") || addMoreReader.equals("no")){
                System.out.println(addMoreReader);
                addingReader = false;
            }
        }
        return readers;
    }
}
