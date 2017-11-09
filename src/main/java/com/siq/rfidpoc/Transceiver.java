package com.siq.rfidpoc;

import com.google.gson.Gson;
import com.impinj.octane.Tag;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Scanner;


public class Transceiver {
    private static String propertiesFilePath;
    private static Properties p = new Properties();
    private static String[] whiteListIds;
    private static long minIntervalMicro;
    private static Dictionary<String, Long> tagLastSeen = new Hashtable<String, Long>();

    private static void ParseCommandLine(String[] args) throws ParseException, IOException {

        Options options = new Options();
        options.addOption(
            Option.builder("p").required().hasArg().longOpt("properties").desc("path to properties file").build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = parser.parse(options, args);
        propertiesFilePath = cmdLine.getOptionValue("p");
        p.load(new FileReader(propertiesFilePath));

        whiteListIds = p.getProperty("whiteListIds").split(",");
        minIntervalMicro = Long.valueOf(p.getProperty("minIntervalMicro"));
    }

    private static void SetWhiteListTags() {
        for (String s : whiteListIds) {
            tagLastSeen.put(s, 0l);
        }
    }

    private static boolean isTagSeenRecently(String id, long timeSeen) {
        Long lastSeen = tagLastSeen.get(id);
        if (lastSeen != null) {
            if (timeSeen - lastSeen < minIntervalMicro)
                return true;
        }
        return false;
    }

    private static void updateTagLastSeen(String id, long timeSeen) {
        tagLastSeen.put(id, timeSeen);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            Scanner s = new Scanner(System.in);

            // TODO: turn this into optional webservice / udp protocol
            ParseCommandLine(args);
            SetWhiteListTags();
            Gson gson = new Gson();

            System.err.println("Transceiver starting.");

            while (true) {
                String msg = s.nextLine();

                Tag t = gson.fromJson(msg, Tag.class);
                String id = t.getEpc().toString();
                long timestamp = Long.parseLong(t.getLastSeenTime().ToString());
                // should be whitelisted
                if (tagLastSeen.get(id) != null) {
                    if (!isTagSeenRecently(id, timestamp)) {
                        // push message to the hub
                        System.out.println(msg);
                        updateTagLastSeen(id, timestamp);
                    }
                } else {
                    System.err.println("Tag not whitelisted: " + id);
                }

            }
        } catch (Exception ex) {
            System.err.println("Exception occurred\n" + ex.toString());
        }
    }
}
