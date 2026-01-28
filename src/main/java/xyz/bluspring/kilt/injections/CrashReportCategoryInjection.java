package xyz.bluspring.kilt.injections;

import xyz.bluspring.kilt.util.KiltHelper;

public interface CrashReportCategoryInjection {
    default void applyStackTrace(Throwable trace) {
        throw new RuntimeException("mixin wtf");
    }

    default void setStackTrace(StackTraceElement[] stackTrace) {
        throw KiltHelper.createMixinException(CrashReportCategoryInjection.class, "setStackTrace");
    }
}
