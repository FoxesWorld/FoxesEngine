import org.fusesource.jansi.AnsiConsole;
import org.apache.logging.log4j.LogManager;

public class Logger {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Logger.class);

    public static void main(String[] args) {
        // Install Jansi
        AnsiConsole.systemInstall();

        try {
            logger.info("This is an info message");
            logger.warn("This is a warning message");
            logger.error("This is an error message");
        } finally {
            // Uninstall Jansi
            AnsiConsole.systemUninstall();
        }
    }
}
