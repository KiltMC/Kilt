package xyz.bluspring.kilt.injections.nbt;

public interface NbtAccounterInjection {
    default String readUTF(String data) {
        throw new IllegalStateException();
    }
}
