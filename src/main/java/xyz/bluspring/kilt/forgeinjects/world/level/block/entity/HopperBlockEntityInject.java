// TRACKED HASH: debbd63809934e4b473f32625f60d1448d5c238d
package xyz.bluspring.kilt.forgeinjects.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.VanillaHopperItemHandler;
import net.minecraftforge.items.VanillaInventoryCodeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.level.block.entity.HopperBlockEntityInjection;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityInject extends RandomizableContainerBlockEntity implements HopperBlockEntityInjection {
    @Shadow private long tickedGameTime;

    protected HopperBlockEntityInject(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Inject(method = "ejectItems", at = @At("HEAD"), cancellable = true)
    private static void kilt$insertEjectHook(Level level, BlockPos pos, BlockState state, Container sourceContainer, CallbackInfoReturnable<Boolean> cir) {
        if (sourceContainer instanceof HopperBlockEntity hopper && VanillaInventoryCodeHooks.insertHook(hopper))
            cir.setReturnValue(true);
    }

    @Inject(method = "suckInItems", at = @At("HEAD"), cancellable = true)
    private static void kilt$insertExtractHook(Level level, Hopper hopper, CallbackInfoReturnable<Boolean> cir) {
        var ret = VanillaInventoryCodeHooks.extractHook(level, hopper);

        if (ret != null)
            cir.setReturnValue(ret);
    }

    protected IItemHandler createUnSidedHandler() {
        return new VanillaHopperItemHandler((HopperBlockEntity) (Object) this);
    }

    @Override
    public long getLastUpdateTime() {
        return this.tickedGameTime;
    }
}