package cn.edu.nudt.secant.jdriver.tools;

/**
 * Created by huang on 4/12/18.
 * reference, stackoverflow.com/questions/15758685/how-to-write-logs-in-text-file-when-using-java-util-logging-logger
 */
import java.io.IOException;
import java.util.logging.*;

public class JLogger
{
    public static Logger logger = Logger.getLogger("run.log");
    public static FileHandler fh;

    public static Logger methodlogger = Logger.getLogger("method.log");
    public static FileHandler methodfh;


    public static void configure() {
        try {
            // This block configure the logger with handler and formatter

            String dirname = Options.v().getOutput();
            if(!dirname.endsWith("/"))
                dirname += "/";
            String logfile = dirname + "jdriver.log";
            String method_log_file = dirname + "jdriver_methods.log";

            fh = new FileHandler(logfile);
            methodfh = new FileHandler(method_log_file);


            //System.err.println("configure JLogger to save file to " + fh.toString());
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            methodlogger.addHandler(methodfh);
            methodfh.setFormatter(formatter);

            logger.setUseParentHandlers(false);
            methodlogger.setUseParentHandlers(false);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logInfo(String msg) {
        logger.info(msg);
    }

    public static void logSevere(String msg) {
        logger.severe(msg);
    }

    public static void logWarning(String msg) {
        logger.warning(msg);
    }

    public static void logMethodInfo(String msg) {methodlogger.info(msg);}

}
