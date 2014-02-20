package mb.robocode.util;

public class Logger {
  
  public static enum LogLevel {
    DEBUG(4), INFO(3), WARN(2), ERROR(1);
    
    private final int priority;
    
    private LogLevel(final int priority) {
      this.priority = priority;
    }
  }
  
  public static final LogLevel LEVEL = LogLevel.DEBUG;
  
  public static void log(final String message, final LogLevel level) {
    if (level.priority <= LEVEL.priority) {
      System.out.println(message);
    }
  }
  
  public static void debug(final String message) {
    log(message, LogLevel.DEBUG);
  }
  
  public static void info(final String message) {
    log(message, LogLevel.INFO);
  }
  
  public static void warn(final String message) {
    log(message, LogLevel.WARN);
  }
  
  public static void error(final String message) {
    log(message, LogLevel.ERROR);
  }

}
