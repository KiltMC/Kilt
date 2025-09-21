package xyz.bluspring.kilt.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Processed by Kilt's Gradle scripts to automatically be written to the fabric.mod.json for injected interfaces.
 * Must still be applied manually via mixin in order to work correctly!
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface FabricInjectedInterface {
    /**
     * Represents the exact class that is to be injected onto.
     * @return The class to be injected onto
     */
    Class<?>[] value() default {};

    /**
     * For use in cases where the class is inaccessible in some way, and an access widener is undesirable.
     * @return The fully qualified class name
     */
    String[] target() default {};
}
