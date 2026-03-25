package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.item.BlockItemInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(BlockItem.class)
public abstract class BlockItemInject extends Item implements BlockItemInjection {
    @Shadow @Final @Deprecated private Block block;

    public BlockItemInject(Properties properties) {
        super(properties);
    }

    @WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType kilt$tryUseForgeSoundType(BlockState instance, Operation<SoundType> original, @Local Level level, @Local BlockPos pos, @Local Player player) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), IForgeBlock.class, "getSoundType", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return instance.getSoundType(level, pos, player);
        }

        return original.call(instance);
    }

    @WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;getPlaceSound(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent kilt$tryUseForgePlaceSound(BlockItem instance, BlockState state, Operation<SoundEvent> original, @Local Level level, @Local BlockPos pos, @Local Player player) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), IForgeBlock.class, "getSoundType", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)
        || KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), BlockItem.class, "getPlaceSound", BlockState.class, Level.class, BlockPos.class, Player.class)) {
            return this.getPlaceSound(state, level, pos, player);
        }

        return original.call(instance, state);
    }

    @Override
    public SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
        return state.getSoundType(world, pos, entity).getPlaceSound();
    }

    @ModifyReturnValue(method = "getBlock", at = @At("RETURN"))
    private Block kilt$tryGetDelegate(Block original) {
        if (original == null)
            return null;

        var delegate = ForgeRegistries.BLOCKS.getDelegate(original);
        if (delegate.isPresent()) {
            return delegate.get().value();
        }

        return original;
    }

    private Block getBlockRaw() {
        return this.block;
    }

    // Kilt: removeFromBlockToItemMap method implemented by Porting Lib
}
