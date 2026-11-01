package kwee.gnucashcharts.library;

import ch.qos.logback.classic.Logger;
import java.util.logging.Level;
import org.slf4j.LoggerFactory;

public class LogLevelChanger {

  public static void setLogLevel(String loggerName, String level) {
    Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
    logger.setLevel(fromJulToLogback(level));
  }

  public static void setLogLevel(Class<?> clazz, String level) {
    setLogLevel(clazz.getName(), level);
  }

  public static ch.qos.logback.classic.Level fromJulToLogback(Level julLevel) {
    if (julLevel == Level.SEVERE)
      return ch.qos.logback.classic.Level.ERROR;
    if (julLevel == Level.WARNING)
      return ch.qos.logback.classic.Level.WARN;
    if (julLevel == Level.INFO)
      return ch.qos.logback.classic.Level.INFO;
    if (julLevel == Level.CONFIG)
      return ch.qos.logback.classic.Level.INFO;
    if (julLevel == Level.FINE)
      return ch.qos.logback.classic.Level.DEBUG;
    if (julLevel == Level.FINER || julLevel == Level.FINEST) {
      return ch.qos.logback.classic.Level.TRACE;
    }
    return ch.qos.logback.classic.Level.INFO;
  }

  public static Level fromLogbackToJul(ch.qos.logback.classic.Level logbackLevel) {
    if (logbackLevel == ch.qos.logback.classic.Level.ERROR)
      return Level.SEVERE;
    if (logbackLevel == ch.qos.logback.classic.Level.WARN)
      return Level.WARNING;
    if (logbackLevel == ch.qos.logback.classic.Level.INFO)
      return Level.INFO;
    if (logbackLevel == ch.qos.logback.classic.Level.DEBUG)
      return Level.FINE;
    if (logbackLevel == ch.qos.logback.classic.Level.TRACE)
      return Level.FINEST;
    return Level.INFO;
  }

  public static ch.qos.logback.classic.Level fromJulToLogback(String julLevel) {
    return fromJulToLogback(Level.parse(julLevel));
  }

  public static Level fromLogbackToJul(String logbackLevel) {
    return fromLogbackToJul(ch.qos.logback.classic.Level.toLevel(logbackLevel));
  }

}
