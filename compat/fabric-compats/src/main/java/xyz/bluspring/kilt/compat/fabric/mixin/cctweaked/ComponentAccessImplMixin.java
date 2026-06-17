package xyz.bluspring.kilt.compat.fabric.mixin.cctweaked;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.compat.fabric.cctweaked.ComponentAccessImplExt;

import java.util.function.Consumer;

@Mixin(targets = "dan200.computercraft.shared.platform.PlatformHelperImpl$ComponentAccessImpl")
public abstract class ComponentAccessImplMixin implements ComponentAccessImplExt {
    @Shadow
    protected abstract ServerLevel getLevel();

    @Shadow
    @Final
    private BlockEntity owner;
    @Unique
    private BlockCapability<?, Direction> kilt$capability;
    @Unique
    private Consumer<Direction> kilt$invalidate;
    @Unique
    BlockCapabilityCache<?, Direction>[] kilt$caches = new BlockCapabilityCache[6];

    @Unique
    @Override
    public void kilt$initializeCapabilityLookups(BlockCapability<?, Direction> capability, Consumer<Direction> invalidate) {
        kilt$capability = capability;
        kilt$invalidate = invalidate;
    }

    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    public Object kilt$tryCapabilityLookup(@Nullable Object original, @Local(argsOnly = true, name = "direction") Direction direction) {
        if (original != null || kilt$capability == null)
            return original;

        var level = getLevel();
        var cache = kilt$caches[direction.ordinal()];
        if (cache == null) {
            cache = kilt$caches[direction.ordinal()] = BlockCapabilityCache.create(
                kilt$capability, level, owner.getBlockPos().relative(direction),
                direction.getOpposite(), () -> !owner.isRemoved(), () -> kilt$invalidate.accept(direction)
            );
        }

        return cache.getCapability();
    }
}
