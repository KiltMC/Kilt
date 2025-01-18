package xyz.bluspring.kilt.workarounds;

public @interface GameTestWorkaround {
    int timeoutTicks() default 100;

    String batch() default "defaultBatch";

    int rotationSteps() default 0;

    boolean required() default true;

    String template() default "";

    long setupTicks() default 0L;

    int attempts() default 1;

    int requiredSuccesses() default 1;

    String templateNamespace() default "";
}
