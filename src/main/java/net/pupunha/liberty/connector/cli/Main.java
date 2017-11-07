package net.pupunha.liberty.connector.cli;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestWord;
import de.vandermeer.asciithemes.a7.A7_Grids;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import net.pupunha.liberty.connector.Application;
import net.pupunha.liberty.connector.LibertyAccess;
import net.pupunha.liberty.connector.LibertyConfiguration;
import net.pupunha.liberty.connector.constants.MBeanConstants;
import net.pupunha.liberty.connector.constants.ProfileConstants;
import net.pupunha.liberty.connector.exception.LibertyAccessException;
import org.apache.commons.cli.*;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.console.ConsoleTextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.pupunha.liberty.connector.Application.*;
import static net.pupunha.liberty.connector.Application.Operation.*;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        LibertyAccess libertyAccess = new LibertyAccess();
        TextTerminal terminal = new ConsoleTextTerminal();
        TextIO textIO = new TextIO(terminal);

        LibertyConfiguration configuration = new LibertyConfiguration();

        String wlpUserDir = System.getenv(ProfileConstants.WLP_USER_DIR);
        if(wlpUserDir == null) {
            if (!configuration.existFile()) {
                String profilePath = textIO.newStringInputReader().read("Input profile path:");
                configuration.setProfilePath(profilePath);
                configuration.save();
            } else {
                configuration.load();
            }
        } else {
            configuration.setProfilePath(wlpUserDir);
        }

        Option list = Option.builder("l")
                .longOpt("list")
                .hasArg()
                .argName("profile")
                .desc("List all applications with status")
                .build();

        Option help = Option.builder("h")
                .longOpt("help")
                .desc("List available commands")
                .build();

        Options options = new Options();
        options.addOption(list);
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse( options, args );
            if (line.hasOption( "l" )) {
                Map<String, File> serversProfile = libertyAccess.getServersProfile(configuration);
                String profile = line.getOptionValue("l");
                if (serversProfile.get(profile) == null) {
                    System.err.println("Profile not found");
                } else {
                    configuration.setProfileUse(profile);
                    printApplicationMBean(terminal, configuration, libertyAccess);
                }
            } else if (line.hasOption( "h" )) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "Liberty Connector", options );
            } else {

                Map<String, File> serversProfile = libertyAccess.getServersProfile(configuration);
                String profileUse = textIO.newStringInputReader()
                        .withNumberedPossibleValues(serversProfile.keySet().toArray(new String[]{}))
                        .read("Servers:");
                configuration.setProfileUse(profileUse);

                MainOption option = textIO.newEnumInputReader(MainOption.class)
                        .read("What do you want connect?");
                if (MainOption.LIST_APPLICATION_STATUS.equals(option)) {
                    printApplicationMBean(terminal, configuration, libertyAccess);
                } else if (MainOption.START_APPLICATION.equals(option)) {
                    invokeOperationApplication(textIO, configuration, libertyAccess, START);
                } else if (MainOption.STOP_APPLICATION.equals(option)) {
                    invokeOperationApplication(textIO, configuration, libertyAccess, STOP);
                } else if (MainOption.RESTART_APPLICATION.equals(option)) {
                    invokeOperationApplication(textIO, configuration, libertyAccess, RESTART);
                }

            }
        }
        catch( ParseException exp ) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        } catch (LibertyAccessException e) {
            log.error(e.getMessage(), e);
        }

    }

    private static void invokeOperationApplication(TextIO textIO, LibertyConfiguration configuration, LibertyAccess libertyAccess, Operation operation) throws LibertyAccessException {
        List<Application> applications = libertyAccess.getApplications(configuration);
        List<String> collect = applications.stream()
                .map(Application::getName)
                .collect(Collectors.toList());
        String applicationSelected = textIO.newStringInputReader()
                .withNumberedPossibleValues(collect)
                .read("Application to "+operation+":");

        Application application = applications.stream()
                .filter(a -> a.getName().equals(applicationSelected))
                .findFirst().orElse(null);
        if (application != null) {
            libertyAccess.invokeOperationApplication(configuration, application, operation);
        }
    }

    private static void printApplicationMBean(TextTerminal terminal, LibertyConfiguration configuration, LibertyAccess libertyAccess) throws LibertyAccessException {
        AsciiTable at = new AsciiTable();
        at.getContext().setGrid(A7_Grids.minusBarPlusEquals());
        at.getRenderer().setCWC(new CWC_LongestWord());

        List<Application> applications = libertyAccess.getApplications(configuration);
        at.addRule();
        at.addRow(MBeanConstants.ATTRIBUTES_APPLICATIONS).setTextAlignment(TextAlignment.CENTER);
        at.addRule();
        applications.forEach(application -> at.addRow(application.getPid(), application.getName(), application.getState()));
        at.addRule();
        terminal.println(at.render());
    }

}
