package xyz.bluspring.kilt.helpers;

/**
 * An annotation to locate which methods have been reimplemented by Porting Lib or related,
 * to add an "implements" on that class.
 */
public @interface FabricImplemented {
    Class<?> value();
}
