package xyz.bluspring.kilt.helpers.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used internally by Kilt for adding new static fields and methods that are inaccessible within
 * Forge/Kilt itself, but are used in Forge mods as a result of Forge's patches.
 * <p>
 * This functions by adding the annotated static field/method in the mixin into the actual class,
 * and promoting it to be public.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CreateStatic {
}
