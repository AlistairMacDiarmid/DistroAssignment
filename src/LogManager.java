import java.util.logging.*;
import java.io.IOException;
import java.nio.file.*;

/**
 * LogManager is a utility class that configures and provides logging services
 * for the application. It ensures that all log messages are written to a
 * log file in a synchronized manner, preventing issues in multithreaded
 * environments. The log file is named "distro_log.txt", and it is created
 * and appended to automatically
 *
 * follows the Singleton pattern to configure logging only once
 * and provides a thread-safe mechanism for writing logs to a file
 */
public class LogManager {
    //singleton flag to ensure logging is configured only once
    private static boolean configured = false;

    //the log file name
    private static final String LOG_FILE = "distro_log.txt";

    //synchronised lock to ensure safe concurrent file writing
    private static final Object fileLock = new Object();

    /**
     * retrieves a logger for the calling class, dynamically determining the class name.
     * @return Logger for the calling class
     */
    public static Logger getLogger() {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        return getLogger(className);
    }

    /**
     * retrieves a named logger instance and ensures the global logging configuration is only applied once
     * @param name the name of the logger
     * @return Logger instance
     */
    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        if (!configured) {
            configureLogger(); //apply the one-time logger configuration
            configured = true;
        }
        return logger;
    }

    /**
     * configures logging by:
     * - removing default handlers
     * - adding a custom file handler for synchronized logging
     * - applying custom formatting for log messages
     */
    private static void configureLogger() {
        try {
            //remove existing handlers to prevent duplicate logging
            Logger rootLogger = Logger.getLogger("");
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            //the custom handler for synchronized file logging
            rootLogger.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    synchronized (fileLock) { //ensure exclusive file access
                        try {
                            String logMessage = getFormatter().format(record);
                            Files.write(Paths.get(LOG_FILE),
                                    logMessage.getBytes(),
                                    StandardOpenOption.CREATE, //create log file if it doesn't exist
                                    StandardOpenOption.APPEND);//append logs to the file
                        } catch (IOException e) {
                            System.err.println("Log write failed: " + e.getMessage());
                        }
                    }
                }

                @Override public void flush() {} //not needed, so not implemented
                @Override public void close() {} //not needed, so not implemented

                //custom log message format: [timestamp] message
                @Override
                public Formatter getFormatter() {
                    return new SimpleFormatter() {
                        private final String format = "[%1$tF %1$tT] %2$s%n";
                        @Override
                        public synchronized String format(LogRecord lr) {
                            return String.format(format, new java.util.Date(lr.getMillis()), lr.getMessage());
                        }
                    };
                }
            });

            //set log level to allow all log levels
            rootLogger.setLevel(Level.ALL);

        } catch (Exception e) {
            //log any errors that occur during logger configuration
            System.err.println("Logger configuration failed: " + e.getMessage());
        }
    }

    /**
     * clears the log file, removing all previous logs.
     * ensures the log file is cleared in a thread-safe manner.
     */
    public static void clearLogs() {
        synchronized (fileLock) {
            try {
                Files.deleteIfExists(Paths.get(LOG_FILE)); // Delete the existing log file if present
            } catch (IOException e) {
                System.err.println("Failed to clear logs: " + e.getMessage());
            }
        }
    }
}