package com.siq.rfidpoc;

import com.impinj.octane.*;
import org.apache.commons.cli.*;

import java.util.Scanner;

public class TagReader {

    private static String hostname;

    private static void ParseCommandLine(String[] args) throws ParseException {

        Options options = new Options();
        options.addOption( Option.builder("h")
            .required()
            .hasArg()
            .longOpt("reader-hostname")
            .desc("The hostname or IP address of the rfid reader")
            .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = parser.parse(options, args);
        hostname = cmdLine.getOptionValue("h");
    }

    public static void main( String[] args )
    {
        try {
            ParseCommandLine(args);

            ImpinjReader reader = new ImpinjReader();

            System.err.println("Connecting");
            reader.connect(hostname);

            Settings settings = reader.queryDefaultSettings();

            ReportConfig report = settings.getReport();
            report.setIncludeAntennaPortNumber(true);
            report.setMode(ReportMode.Individual);
            report.setIncludeLastSeenTime(true);

            // The reader can be set into various modes in which reader
            // dynamics are optimized for specific regions and environments.
            // The following mode, AutoSetDenseReader, monitors RF noise and interference and then automatically
            // and continuously optimizes the reader’s configuration
            settings.setReaderMode(ReaderMode.AutoSetDenseReader);

            // set some special settings for antenna 1
            AntennaConfigGroup antennas = settings.getAntennas();
            antennas.disableAll();
            antennas.enableById(new short[]{1,2});
            antennas.getAntenna((short) 1).setIsMaxRxSensitivity(false);
            antennas.getAntenna((short) 1).setIsMaxTxPower(false);
            antennas.getAntenna((short) 1).setTxPowerinDbm(20.0);
            antennas.getAntenna((short) 1).setRxSensitivityinDbm(-70);

            antennas.getAntenna((short) 2).setIsMaxRxSensitivity(false);
            antennas.getAntenna((short) 2).setIsMaxTxPower(false);
            antennas.getAntenna((short) 2).setTxPowerinDbm(20.0);
            antennas.getAntenna((short) 2).setRxSensitivityinDbm(-70);

            reader.setTagReportListener(new TagReportListenerImplementation());

            System.err.println("Applying Settings");
            reader.applySettings(settings);

            System.err.println("Starting");
            reader.start();

            System.err.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();
            s.close();

            reader.stop();
            reader.disconnect();

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
}
