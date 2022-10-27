package zeymo.protocol.dump;

import java.util.Locale;

/**
 * @author Zeymo
 */
public class OSUtils {
    private static final String OPERATING_SYSTEM_NAME;
    static PlatformEnum platform;

    private OSUtils() {
    }

    public static boolean isWindows() {
        return platform == PlatformEnum.WINDOWS;
    }

    public static boolean isLinux() {
        return platform == PlatformEnum.LINUX;
    }

    public static boolean isMac() {
        return platform == PlatformEnum.MACOSX;
    }

    public static boolean isCygwinOrMinGW() {
        return isWindows() && (System.getenv("MSYSTEM") != null && System.getenv("MSYSTEM").startsWith("MINGW") || "/bin/bash".equals(System.getenv("SHELL")));
    }

    static {
        OPERATING_SYSTEM_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (OPERATING_SYSTEM_NAME.startsWith("linux")) {
            platform = PlatformEnum.LINUX;
        } else if (!OPERATING_SYSTEM_NAME.startsWith("mac") && !OPERATING_SYSTEM_NAME.startsWith("darwin")) {
            if (OPERATING_SYSTEM_NAME.startsWith("windows")) {
                platform = PlatformEnum.WINDOWS;
            } else {
                platform = PlatformEnum.UNKNOWN;
            }
        } else {
            platform = PlatformEnum.MACOSX;
        }

    }
}
