package premar.tech.facerecognitionapp.utils;

/** Not a real crash reporting library! */
public final class CrashReporter {
    public static void log(int priority, String tag, String message) {
        // TODO add log entry to circular buffer.
    }

    public static void logWarning(Throwable t) {
        // TODO report non-fatal warning.
    }

    public static void logError(Throwable t) {
        // TODO report non-fatal error.
    }

    private CrashReporter() {
        throw new AssertionError("No instances.");
    }
}