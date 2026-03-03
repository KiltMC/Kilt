package xyz.bluspring.kilt.workarounds;

public interface ForgeFluidStackWorkaround {
    default int getAmount() {
        throw new IllegalStateException("ForgeFluidStackWorkaround failed to apply!");
    }
}
