import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

public class EnhancedLoggerUtil {
    private static final String CSV_FILE_NAME = generateLogFileName();
    private static boolean csvInitialized = false;
    private static final ReentrantLock csvLock = new ReentrantLock();

    private static String generateLogFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "conversation_log_" + timestamp + ".csv";
    }

    // Log levels
    public enum LogLevel {
        INFO, WARNING, ERROR, DEBUG
    }

    public static void logMessage(String sender, String receiver, String performative,
                                  String conversationId, String content) {
        logMessage(sender, receiver, performative, conversationId, content, LogLevel.INFO);
    }

    public static void logMessage(String sender, String receiver, String performative,
                                  String conversationId, String content, LogLevel level) {
        // Log to CSV
        logToCSV(sender, receiver, performative, conversationId, content, level);

        // Console output for debugging
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.printf("[%s] %s -> %s: %s (Conv: %s) [%s]%n",
                timestamp, sender, receiver, performative, conversationId, level);
    }

    private static void logToCSV(String sender, String receiver, String performative,
                                 String conversationId, String content, LogLevel level) {
        csvLock.lock();
        try (FileWriter fw = new FileWriter(CSV_FILE_NAME, true)) {
            if (!csvInitialized) {
                fw.append("timestamp,sender,receiver,performative,conversationId,content,level\n");
                csvInitialized = true;
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String escapedContent = content.replace(",", ";").replace("\n", " ");

            fw.append(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                    timestamp, sender, receiver, performative, conversationId, escapedContent, level));
            fw.flush();
        } catch (IOException e) {
            System.err.println("Error writing to CSV log: " + e.getMessage());
            e.printStackTrace();
        } finally {
            csvLock.unlock();
        }
    }



    // Log system events
    public static void logSystemEvent(String event, String details) {
        logSystemEvent(event, details, LogLevel.INFO);
    }

    public static void logSystemEvent(String event, String details, LogLevel level) {
        logMessage("SYSTEM", "SYSTEM", event, "system", details, level);
    }

    // Log errors
    public static void logError(String sender, String receiver, String error, String conversationId) {
        logMessage(sender, receiver, "ERROR", conversationId, error, LogLevel.ERROR);
    }

    // Log warnings
    public static void logWarning(String sender, String receiver, String warning, String conversationId) {
        logMessage(sender, receiver, "WARNING", conversationId, warning, LogLevel.WARNING);
    }

    // Log debug information
    public static void logDebug(String sender, String receiver, String debugInfo, String conversationId) {
        logMessage(sender, receiver, "DEBUG", conversationId, debugInfo, LogLevel.DEBUG);
    }

    // Get conversation summary
    public static void generateConversationSummary(String conversationId) {
        // This would typically read the log files and generate a summary
        // For now, we'll just log that we're generating a summary
        logSystemEvent("SUMMARY_REQUEST", "Generating summary for conversation: " + conversationId);
    }

    // Clean up old logs (older than specified days)
    public static void cleanupOldLogs(int daysToKeep) {
        logSystemEvent("CLEANUP", "Cleaning up logs older than " + daysToKeep + " days");
        // Implementation would go here
    }

    // Get current log file name
    public static String getCurrentLogFileName() {
        return CSV_FILE_NAME;
    }

    // Log system startup
    public static void logSystemStartup() {
        logSystemEvent("SYSTEM_STARTUP", "Multi-Agent Booking System started. Log file: " + CSV_FILE_NAME);
    }
}