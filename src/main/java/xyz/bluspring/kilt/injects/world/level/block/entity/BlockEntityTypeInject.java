package xyz.bluspring.kilt.injects.world.level.block.entity;

import java.util.Collections;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.block.entity.BlockEntityTypeInjection;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeInject<T extends BlockEntity> implements BlockEntityTypeInjection<T> {
    @Shadow @Final private Set<Block> validBlocks;

    @Unique private boolean onlyOpCanSetNbt = false;

    public BlockEntityTypeInject(final BlockEntityType.BlockEntitySupplier<? extends T> factory, final Set<Block> validBlocks) {}

    @CreateInitializer
    public BlockEntityTypeInject(final BlockEntityType.BlockEntitySupplier<? extends T> factory, final Set<Block> validBlocks, boolean onlyOpCanSetNbt) {
        this(factory, validBlocks);
        this.onlyOpCanSetNbt = onlyOpCanSetNbt;
    }

    @CreateInitializer
    public BlockEntityTypeInject(final BlockEntityType.BlockEntitySupplier<? extends T> factory, Block... validBlocks) {
        this(factory, false, validBlocks);
    }

    @CreateInitializer
    public BlockEntityTypeInject(final BlockEntityType.BlockEntitySupplier<? extends T> factory, boolean onlyOpCanSetNbt, Block... validBlocks) {
        this(factory, Set.of(validBlocks), onlyOpCanSetNbt);
        if (validBlocks.length == 0)
            throw new IllegalArgumentException("Block entity type instantiated without valid blocks. If this is intentional, pass Set.of() instead of an empty vararg.");
    }

    @Override
    public Set<Block> getValidBlocks() {
        return Collections.unmodifiableSet(this.validBlocks);
    }

    @Inject(method = "onlyOpCanSetNbt", at = @At("HEAD"), cancellable = true)
    private void kilt$checkOnlyOpCanSetNbt(CallbackInfoReturnable<Boolean> cir) {
        if (this.onlyOpCanSetNbt)
            cir.setReturnValue(true);
    }

    @Override
    public void kilt$setOnlyOpCanSetNbt(boolean value) {
        this.onlyOpCanSetNbt = value;
    }
}
