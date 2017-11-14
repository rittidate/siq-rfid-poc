package com.siq.rfidpoc;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class MultipleReaders {
    static ArrayList<ImpinjReader> readers = new ArrayList<ImpinjReader>();
    static ArrayList<ReaderWrapper> inputReaders = new ArrayList<ReaderWrapper>();
    private static boolean addingReader = true;

    public static void main(String[] args) {

        while(addingReader){
            Scanner inputReader = new Scanner(System.in);
            System.out.println("Add reader hostname or IP address...");
            ReaderWrapper readerWrapper = new ReaderWrapper();
            readerWrapper.ipAddress = inputReader.nextLine();
            System.out.println("Add name reader...");
            readerWrapper.name = inputReader.nextLine();
            inputReaders.add(readerWrapper);
            System.out.println(inputReaders.size());

            System.out.println("Do you want to continue add reader? [Y/N]");
            String addMoreReader = inputReader.nextLine().toLowerCase();

            if(addMoreReader.equals("y") || addMoreReader.equals("yes")) {

            }else if(addMoreReader.equals("n") || addMoreReader.equals("no")){
                System.out.println(addMoreReader);
                addingReader = false;
            }
        }

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
        Scanner s = new Scanner(System.in);
        s.nextLine();

        for (int i = 0; i < readers.size(); i++) {

            try {
                ImpinjReader reader = readers.get(i);
                reader.stop();
                reader.disconnect();
            } catch (OctaneSdkException ex) {
                System.out.println("Failed to stop reader: " + ex.getMessage());
            }

        }
    }
}
