package zeymo.protocol.dump;

import java.util.logging.Level;
import java.util.regex.Matcher;

/**
 * @author Zeymo
 */
public class AnsiLog {
    static boolean enableColor;
    public static Level LEVEL;
    private static final String RESET = "\u001b[0m";
    private static final int DEFAULT = 39;
    private static final int BLACK = 30;
    private static final int RED = 31;
    private static final int GREEN = 32;
    private static final int YELLOW = 33;
    private static final int BLUE = 34;
    private static final int MAGENTA = 35;
    private static final int CYAN = 36;
    private static final int WHITE = 37;
    private static final String TRACE_PREFIX = "[TRACE] ";
    private static final String TRACE_COLOR_PREFIX;
    private static final String DEBUG_PREFIX = "[DEBUG] ";
    private static final String DEBUG_COLOR_PREFIX;
    private static final String INFO_PREFIX = "[INFO] ";
    private static final String INFO_COLOR_PREFIX;
    private static final String WARN_PREFIX = "[WARN] ";
    private static final String WARN_COLOR_PREFIX;
    private static final String ERROR_PREFIX = "[ERROR] ";
    private static final String ERROR_COLOR_PREFIX;

    private AnsiLog() {
    }

    public static boolean enableColor() {
        return enableColor;
    }

    public static Level level(Level level) {
        Level old = LEVEL;
        LEVEL = level;
        return old;
    }

    public static Level level() {
        return LEVEL;
    }

    public static String black(String msg) {
        return enableColor ? colorStr(msg, 30) : msg;
    }

    public static String red(String msg) {
        return enableColor ? colorStr(msg, 31) : msg;
    }

    public static String green(String msg) {
        return enableColor ? colorStr(msg, 32) : msg;
    }

    public static String yellow(String msg) {
        return enableColor ? colorStr(msg, 33) : msg;
    }

    public static String blue(String msg) {
        return enableColor ? colorStr(msg, 34) : msg;
    }

    public static String magenta(String msg) {
        return enableColor ? colorStr(msg, 35) : msg;
    }

    public static String cyan(String msg) {
        return enableColor ? colorStr(msg, 36) : msg;
    }

    public static String white(String msg) {
        return enableColor ? colorStr(msg, 37) : msg;
    }

    private static String colorStr(String msg, int colorCode) {
        return "\u001b[" + colorCode + "m" + msg + "\u001b[0m";
    }

    public static void trace(String msg) {
        if (canLog(Level.FINEST)) {
            if (enableColor) {
                System.out.println(TRACE_COLOR_PREFIX + msg);
            } else {
                System.out.println("[TRACE] " + msg);
            }
        }

    }

    public static void trace(String format, Object... arguments) {
        if (canLog(Level.FINEST)) {
            trace(format(format, arguments));
        }

    }

    public static void trace(Throwable t) {
        if (canLog(Level.FINEST)) {
            t.printStackTrace(System.out);
        }

    }

    public static void debug(String msg) {
        if (canLog(Level.FINER)) {
            if (enableColor) {
                System.out.println(DEBUG_COLOR_PREFIX + msg);
            } else {
                System.out.println("[DEBUG] " + msg);
            }
        }

    }

    public static void debug(String format, Object... arguments) {
        if (canLog(Level.FINER)) {
            debug(format(format, arguments));
        }

    }

    public static void debug(Throwable t) {
        if (canLog(Level.FINER)) {
            t.printStackTrace(System.out);
        }

    }

    public static void info(String msg) {
        if (canLog(Level.CONFIG)) {
            if (enableColor) {
                System.out.println(INFO_COLOR_PREFIX + msg);
            } else {
                System.out.println("[INFO] " + msg);
            }
        }

    }

    public static void info(String format, Object... arguments) {
        if (canLog(Level.CONFIG)) {
            info(format(format, arguments));
        }

    }

    public static void info(Throwable t) {
        if (canLog(Level.CONFIG)) {
            t.printStackTrace(System.out);
        }

    }

    public static void warn(String msg) {
        if (canLog(Level.WARNING)) {
            if (enableColor) {
                System.out.println(WARN_COLOR_PREFIX + msg);
            } else {
                System.out.println("[WARN] " + msg);
            }
        }

    }

    public static void warn(String format, Object... arguments) {
        if (canLog(Level.WARNING)) {
            warn(format(format, arguments));
        }

    }

    public static void warn(Throwable t) {
        if (canLog(Level.WARNING)) {
            t.printStackTrace(System.out);
        }

    }

    public static void error(String msg) {
        if (canLog(Level.SEVERE)) {
            if (enableColor) {
                System.out.println(ERROR_COLOR_PREFIX + msg);
            } else {
                System.out.println("[ERROR] " + msg);
            }
        }

    }

    public static void error(String format, Object... arguments) {
        if (canLog(Level.SEVERE)) {
            error(format(format, arguments));
        }

    }

    public static void error(Throwable t) {
        if (canLog(Level.SEVERE)) {
            t.printStackTrace(System.out);
        }

    }

    private static String format(String from, Object... arguments) {
        if (from == null) {
            return null;
        } else {
            String computed = from;
            if (arguments != null && arguments.length != 0) {
                Object[] var3 = arguments;
                int var4 = arguments.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    Object argument = var3[var5];
                    computed = computed.replaceFirst("\\{\\}", Matcher.quoteReplacement(argument.toString()));
                }
            }

            return computed;
        }
    }

    private static boolean canLog(Level level) {
        return level.intValue() >= LEVEL.intValue();
    }

    static {
        LEVEL = Level.CONFIG;
        TRACE_COLOR_PREFIX = "[" + colorStr("TRACE", 32) + "] ";
        DEBUG_COLOR_PREFIX = "[" + colorStr("DEBUG", 32) + "] ";
        INFO_COLOR_PREFIX = "[" + colorStr("INFO", 32) + "] ";
        WARN_COLOR_PREFIX = "[" + colorStr("WARN", 33) + "] ";
        ERROR_COLOR_PREFIX = "[" + colorStr("ERROR", 31) + "] ";
        if (System.console() != null) {
            enableColor = true;
            if (OSUtils.isWindows()) {
                enableColor = false;
            }
        }

        if (OSUtils.isCygwinOrMinGW()) {
            enableColor = true;
        }

    }
}
