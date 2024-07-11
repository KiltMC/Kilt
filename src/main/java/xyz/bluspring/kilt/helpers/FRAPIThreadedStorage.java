package xyz.bluspring.kilt.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public class FRAPIThreadedStorage {
    public static final ThreadLocal<BlockAndTintGetter> LEVEL = ThreadLocal.withInitial(() -> null);
    public static final ThreadLocal<BlockPos> POS = ThreadLocal.withInitial(() -> null);
}
