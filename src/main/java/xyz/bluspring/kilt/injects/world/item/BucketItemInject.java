package xyz.bluspring.kilt.injects.world.item;

import java.util.Optional;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.extensions.IBucketPickupExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.item.BucketItemInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(BucketItem.class)
public abstract class BucketItemInject extends Item implements BucketItemInjection {
    @Shadow public Fluid content;
    @Shadow public abstract boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult result);

    public BucketItemInject(Properties properties) {
        super(properties);
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BucketPickup;getPickupSound()Ljava/util/Optional;"))
    private Optional<SoundEvent> kilt$tryUseStateAwarePickupSound(BucketPickup instance, Operation<Optional<SoundEvent>> original, @Local BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IBucketPickupExtension.class, "getPickupSound", BlockState.class)) {
            return instance.getPickupSound(state);
        }

        return original.call(instance);
    }

    @Definition(id = "blockState", local = @Local(type = BlockState.class))
    @Definition(id = "getBlock", method = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;")
    @Definition(id = "LiquidBlockContainer", type = LiquidBlockContainer.class)
    @Expression("blockState.getBlock() instanceof LiquidBlockContainer")
    @ModifyExpressionValue(method = "use", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$tryCheckContainsFluid(boolean original, @Local(argsOnly = true) Player player, @Local(argsOnly = true) Level level, @Local(ordinal = 0) BlockPos pos, @Local BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), BucketItem.class, "canBlockContainFluid", Player.class, Level.class, BlockPos.class, BlockState.class)) {
            return this.canBlockContainFluid(player, level, pos, state);
        }

        return original;
    }

    @Definition(id = "content", field = "Lnet/minecraft/world/item/BucketItem;content:Lnet/minecraft/world/level/material/Fluid;")
    @Definition(id = "WATER", field = "Lnet/minecraft/world/level/material/Fluids;WATER:Lnet/minecraft/world/level/material/FlowingFluid;")
    @Expression("this.content == WATER")
    @ModifyExpressionValue(method = "use", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$tryUseAlternativeFluidCheck(boolean original, @Local(argsOnly = true) Player player, @Local(argsOnly = true) Level level, @Local(ordinal = 0) BlockPos pos, @Local BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), BucketItem.class, "canBlockContainFluid", Player.class, Level.class, BlockPos.class, BlockState.class)) {
            return true;
        }

        return original || ((LiquidBlockContainer) state.getBlock()).canPlaceLiquid(player, level, pos, state, this.content);
    }

    @Unique
    private final ThreadLocal<ItemStack> kilt$container = new ThreadLocal<>();

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult result, @Nullable ItemStack container) {
        this.kilt$container.set(container);
        var value = this.emptyContents(player, level, pos, result);
        this.kilt$container.remove();
        return value;
    }

    protected boolean canBlockContainFluid(@Nullable Player player, Level level, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof LiquidBlockContainer && ((LiquidBlockContainer) state.getBlock()).canPlaceLiquid(player, level, pos, state, this.content);
    }
}
