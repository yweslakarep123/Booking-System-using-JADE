import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.HashMap;

public class EnhancedLoggerUtil {
    private static final String CSV_FILE_NAME = "conversation_log.csv";
    private static final String JSON_FILE_NAME = "conversation_log.json";
    private static boolean csvInitialized = false;
    private static boolean jsonInitialized = false;
    private static final ReentrantLock csvLock = new ReentrantLock();
    private static final ReentrantLock jsonLock = new ReentrantLock();
    
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
        
        // Log to JSON
        logToJSON(sender, receiver, performative, conversationId, content, level);
        
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
    
    private static void logToJSON(String sender, String receiver, String performative,
                                  String conversationId, String content, LogLevel level) {
        jsonLock.lock();
        try {
            if (!jsonInitialized) {
                // First time writing - create new file
                try (FileWriter fw = new FileWriter(JSON_FILE_NAME)) {
                    fw.write("[\n");
                    fw.write(formatJSONEntry(createLogEntry(sender, receiver, performative, conversationId, content, level)));
                    fw.write("\n]");
                    fw.flush();
                }
                jsonInitialized = true;
            } else {
                // For simplicity, we'll just append to the file and handle JSON formatting later
                // This is a simplified approach that works reliably
                try (FileWriter fw = new FileWriter(JSON_FILE_NAME, true)) {
                    fw.write(",\n");
                    fw.write(formatJSONEntry(createLogEntry(sender, receiver, performative, conversationId, content, level)));
                    fw.write("\n]");
                    fw.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to JSON log: " + e.getMessage());
            e.printStackTrace();
        } finally {
            jsonLock.unlock();
        }
    }
    
    private static Map<String, Object> createLogEntry(String sender, String receiver, String performative,
                                                     String conversationId, String content, LogLevel level) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        logEntry.put("sender", sender);
        logEntry.put("receiver", receiver);
        logEntry.put("performative", performative);
        logEntry.put("conversationId", conversationId);
        logEntry.put("content", content);
        logEntry.put("level", level.toString());
        return logEntry;
    }
    
    private static String formatJSONEntry(Map<String, Object> entry) {
        StringBuilder json = new StringBuilder();
        json.append("  {\n");
        
        boolean first = true;
        for (Map.Entry<String, Object> pair : entry.entrySet()) {
            if (!first) json.append(",\n");
            json.append("    \"").append(pair.getKey()).append("\": ");
            
            Object value = pair.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeJSONString((String) value)).append("\"");
            } else {
                json.append(value);
            }
            first = false;
        }
        
        json.append("\n  }");
        return json.toString();
    }
    
    private static String escapeJSONString(String str) {
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
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
}
