package xyz.bluspring.kilt.injections;

public interface CrashReportCategoryInjection {
    default void applyStackTrace(Throwable trace) {
        throw new RuntimeException("mixin wtf");
    }
}
