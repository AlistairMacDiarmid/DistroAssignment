import java.util.logging.*;
import java.io.IOException;
import java.nio.file.*;

/**
 * thread-safe logging that writes to "distro_log.txt" with timestamp formatting
 * uses singleton pattern to configure logging once
 */
public class LogManager {
    private static final String LOG_FILE = "distro_log.txt";
    private static final Object lock = new Object();
    private static boolean configured = false;

    /**
     * gets a logger for the calling class
     */
    public static Logger getLogger() {
        return getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
    }

    /**
     * Gets a named logger, ensuring one-time initialization
     * @param name Logger name - the class name
     */
    public static Logger getLogger(String name) {
        if (!configured) {
            initLogger();
        }
        return Logger.getLogger(name);
    }

    /**
     * a one-time logger setup with:
     * -file handler for "distro_log.txt"
     * -timestamp formatting
     * -thread-safe writes
     */
    private static synchronized void initLogger() {
        if (configured) return;

        try {
            Logger root = Logger.getLogger("");
            //remove the handler for console logging
            for (Handler h : root.getHandlers()) {
                root.removeHandler(h);
            }

            //add the file handler
            root.addHandler(createFileHandler());
            root.setLevel(Level.ALL);
            configured = true;

        } catch (Exception e) {
            System.err.println("Logger init failed: " + e.getMessage());
        }
    }

    /**
     * creates a thread-safe file handler with timestamp formatting.
     */
    private static Handler createFileHandler() {
        return new Handler() {
            @Override
            public void publish(LogRecord record) {
                synchronized (lock) {
                    try {
                        String msg = String.format("[%1$tF %1$tT] %2$s%n",
                                new java.util.Date(record.getMillis()),
                                record.getMessage());
                        Files.write(Paths.get(LOG_FILE),
                                msg.getBytes(),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        System.err.println("Log write error: " + e.getMessage());
                    }
                }
            }

            @Override public void flush() {}
            @Override public void close() {}
        };
    }

    /**
     * clears the log file thread-safely.
     */
    public static void clearLogs() {
        synchronized (lock) {
            try {
                Files.deleteIfExists(Paths.get(LOG_FILE));
            } catch (IOException e) {
                System.err.println("Clear logs failed: " + e.getMessage());
            }
        }
    }
}