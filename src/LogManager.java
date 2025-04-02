import java.util.logging.*;
import java.io.IOException;
import java.nio.file.*;

public class LogManager {
    //root logger that all loggers inherit from
    private static final Logger rootLogger = Logger.getLogger("");

    //singleton pattern flag to ensure logging is configured only once
    private static boolean configured = false;

    //shared log file name
    private static final String LOG_FILE = "distro_log.txt";

    //global file lock to ensure synchronized file writes across threads/JVMs
    private static final Object fileLock = new Object();

    /**
     * retrieves a logger for the calling class
     * uses stack trace to dynamically determine the caller's class name
     */
    public static Logger getLogger() {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        return getLogger(className);
    }

    /**
     * gets a named logger instance. ensures that the global configuration is applied
     */
    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        if (!configured) {
            configureLogger();//apply one-time configuration
            configured = true;
        }
        return logger;
    }

    /**
     * configures logging:
     * - removes default handlers (console output)
     * - adds a custom file handler for synchronized logging
     * - applies the custom formatting
     */
    private static void configureLogger() {
        try {
            //remove existing handlers to prevent duplicate logging
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            //custom handler to write logs with thread-safety
            rootLogger.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    synchronized (fileLock) { //ensure exclusive file access
                        try {
                            Files.write(Paths.get(LOG_FILE),
                                    (getFormatter().format(record)).getBytes(),
                                    StandardOpenOption.CREATE,  //create if missing
                                    StandardOpenOption.APPEND); //append logs
                        } catch (IOException e) {
                            System.err.println("log write failed: " + e.getMessage());
                        }
                    }
                }

                @Override public void flush() {}  //no buffering needed
                @Override public void close() {}  //no resources to close

                /**
                 * custom log message format: [timestamp] message
                 */
                @Override
                public Formatter getFormatter() {
                    return new SimpleFormatter() {
                        private final String format = "[%1$tF %1$tT] %2$s%n";

                        @Override
                        public synchronized String format(LogRecord lr) {
                            return String.format(format,
                                    new java.util.Date(lr.getMillis()),  //timestamp
                                    lr.getMessage());//log message
                        }
                    };
                }
            });

            //allow all log levels
            rootLogger.setLevel(Level.ALL);

        } catch (Exception e) {
            System.err.println("logger configuration failed: " + e.getMessage());
        }
    }

    /**
     * clear the log file - used by Coordinator to start fresh
     * use synchronized delete to prevent concurrent issues
     */
    public static void clearLogs() {
        synchronized (fileLock) {
            try {
                Files.deleteIfExists(Paths.get(LOG_FILE));
            } catch (IOException e) {
                System.err.println("failed to clear logs: " + e.getMessage());
            }
        }
    }
}