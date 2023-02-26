package xyz.bluspring.kilt.injections.porting_lib;

public interface RegistryObjectInjection {
    default void updateRef() {
        throw new IllegalStateException();
    }
}
