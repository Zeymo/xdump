package zeymo.protocol.dump;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import org.apache.commons.cli.*;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.util.NifSelector;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author Zeymo
 */
public class Bootstrap {
    public static void main(String[] args) throws PcapNativeException {
        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("Print usage").required(false).build());
        options.addOption(Option.builder("i").longOpt("networkInterface").desc("The Network Interface").hasArg().required(false).build());
        options.addOption(Option.builder().longOpt("ignoreHeartbeat").desc("ignore Heartbeat").required(false).build());
        options.addOption(Option.builder().longOpt("host").desc("The host").hasArg().required(false).build());
        options.addOption(Option.builder().longOpt("port").desc("The port").hasArg().required(false).build());
        options.addOption(Option.builder("p").longOpt("protocol").desc("The protocol").hasArg().required(false).build());
        options.addOption(Option.builder("f").longOpt("filter").desc("The BPF filter").hasArg().required(false).build());
        DefaultParser parser = new DefaultParser();

        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            usage(options);
            return;
        }

        if (commandLine == null) {
            usage(options);
            System.exit(1);
            return;
        }

        if (commandLine.hasOption("help")) {
            usage(options);
            System.exit(0);
            return;
        }

        Configuration configuration = new Configuration();
        String protocol = commandLine.getOptionValue("protocol");
        if (!Strings.isNullOrEmpty(protocol)) {
            configuration.setProtocol(protocol);
        }

        if (commandLine.hasOption("ignoreHeartbeat")) {
            configuration.setIgnoreHeartbeat(true);
        }

        String host = commandLine.getOptionValue("host");
        String port = commandLine.getOptionValue("port");
        int portInt = 0;
        if (!Strings.isNullOrEmpty(port)) {
            portInt = Ints.tryParse(port);
        }


        String networkInterface = commandLine.getOptionValue("i");
        PcapNetworkInterface pcapNetworkInterface = null;
        if (!Strings.isNullOrEmpty(networkInterface)) {
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            for (PcapNetworkInterface dev : allDevs) {
                if (dev.getName().equals(networkInterface)) {
                    pcapNetworkInterface = dev;
                }
                if (pcapNetworkInterface == null) {
                    AnsiLog.error("can not find network interface, maybe use 'sudo' for auth: " + networkInterface);
                    System.exit(1);
                }
            }
        } else {
            try {
                pcapNetworkInterface = new NifSelector().selectNetworkInterface();
            } catch (IOException e) {
                e.printStackTrace();
                AnsiLog.error("try to select network interface error!");
                System.exit(1);
            }
        }

        // filter
        String finalFilter = null;
        String filter = commandLine.getOptionValue("f");
        if (!Strings.isNullOrEmpty(filter)) {
            finalFilter = filter;
        } else if (host != null || portInt != 0) {
            finalFilter = "tcp";
            if (host != null) {
                finalFilter = finalFilter + " and host " + host;
            }
            if (portInt != 0) {
                finalFilter = finalFilter + " and port " + port;
            }
        }
        if (finalFilter == null) {
            AnsiLog.error("Please set dump host or port or filter !");
            System.exit(1);
        }

        configuration.setFilter(finalFilter);

        System.out.println("configuration: " + configuration);
        DumpWorker channel = new DumpWorker(pcapNetworkInterface, configuration);
        try {
            channel.start();
        } catch (Throwable t) {
            //ignore
        }
    }

    public static void usage(Options options) {
        HelpFormatter hf = new HelpFormatter();
        final PrintWriter pw = new PrintWriter(System.out);
        hf.printUsage(pw, 110, "", options);
        pw.flush();
    }
}