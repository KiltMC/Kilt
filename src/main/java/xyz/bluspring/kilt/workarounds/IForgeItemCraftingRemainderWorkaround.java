package xyz.bluspring.kilt.workarounds;

public interface IForgeItemCraftingRemainderWorkaround {

    ThreadLocal<Boolean> kilt$isCheckingCraftingItem = ThreadLocal.withInitial(() -> false);

}
