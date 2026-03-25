package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Reference2BooleanLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.extensions.IForgeBlock;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.block.FireBlockInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(FireBlock.class)
public abstract class FireBlockInject extends BaseFireBlock implements FireBlockInjection {
    @Shadow protected abstract void checkBurnOut(Level level, BlockPos pos, int chance, RandomSource random, int age);

    private final ThreadLocal<Direction> kilt$face = new ThreadLocal<>();
    @Unique private static final Reference2BooleanLinkedOpenHashMap<Class<? extends Block>> kilt$cachedFlammabilityOverride = new Reference2BooleanLinkedOpenHashMap<>();
    @Unique private static final Reference2BooleanLinkedOpenHashMap<Class<? extends Block>> kilt$cachedFireEventOverride = new Reference2BooleanLinkedOpenHashMap<>();

    public FireBlockInject(Properties properties, float fireDamage) {
        super(properties, fireDamage);
    }

    @WrapOperation(method = "getStateForPlacement(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FireBlock;canBurn(Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 0))
    private boolean kilt$checkCanCatchFire(FireBlock instance, BlockState state, Operation<Boolean> original, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) BlockPos pos) {
        return original.call(instance, state) || ((FireBlockInjection) instance).canCatchFire(level, pos, Direction.UP);
    }

    @WrapOperation(method = "getStateForPlacement(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FireBlock;canBurn(Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 1))
    private boolean kilt$checkCanCatchFire(FireBlock instance, BlockState state, Operation<Boolean> original, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) BlockPos pos, @Local Direction direction) {
        return original.call(instance, state) || ((FireBlockInjection) instance).canCatchFire(level, pos.relative(direction), direction.getOpposite());
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private boolean kilt$checkIsFireSource(BlockState instance, TagKey<Block> tagKey, Operation<Boolean> original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos) {
        return original.call(instance, tagKey) || instance.isFireSource(level, pos, Direction.UP);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FireBlock;canBurn(Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 0))
    private boolean kilt$checkCanCatchFireTick(FireBlock instance, BlockState state, Operation<Boolean> original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos) {
        return original.call(instance, state) || ((FireBlockInjection) instance).canCatchFire(level, pos.below(), Direction.UP);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FireBlock;checkBurnOut(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/util/RandomSource;I)V"))
    private void kilt$setDirection(FireBlock instance, Level level, BlockPos pos, int chance, RandomSource random, int age, Operation<Void> original, @Local(argsOnly = true) BlockPos originalPos) {
        var relativeX = pos.getX() - originalPos.getX();
        var relativeY = pos.getY() - originalPos.getY();
        var relativeZ = pos.getZ() - originalPos.getZ();

        var direction = Direction.fromDelta(relativeX, relativeY, relativeZ);
        if (direction != null)
            kilt$face.set(direction.getOpposite());
        original.call(instance, level, pos, chance, random, age);
        kilt$face.remove();
    }

    @Intrinsic
    private void tryCatchFire(Level level, BlockPos pos, int chance, RandomSource random, int age, Direction face) {
        kilt$face.set(face);
        this.checkBurnOut(level, pos, chance, random, age);
        kilt$face.remove();
    }

    @WrapOperation(method = "checkBurnOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FireBlock;getBurnOdds(Lnet/minecraft/world/level/block/state/BlockState;)I"))
    private int kilt$checkFlammability(FireBlock instance, BlockState state, Operation<Integer> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        if (kilt$cachedFlammabilityOverride.computeIfAbsent(state.getBlock().getClass(), $ -> KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), IForgeBlock.class, "getFlammability", BlockState.class, BlockGetter.class, BlockPos.class, Direction.class))) {
            return state.getFlammability(level, pos, kilt$face.get());
        }

        return original.call(instance, state);
    }

    @WrapOperation(method = "checkBurnOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private Block kilt$callCaughtFire(BlockState instance, Operation<Block> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        var block = original.call(instance);

        if (block == null)
            return null;

        if (kilt$cachedFireEventOverride.computeIfAbsent(block.getClass(), $ -> KiltHelper.INSTANCE.hasMethodOverride(block.getClass(), IForgeBlock.class, "onCaughtFire", BlockState.class, Level.class, BlockPos.class, Direction.class, LivingEntity.class))) {
            instance.onCaughtFire(level, pos, kilt$face.get(), null);
            return null;
        }

        return block;
    }

    @WrapOperation(method = "getIgniteOdds(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FireBlock;getIgniteOdds(Lnet/minecraft/world/level/block/state/BlockState;)I"))
    private int kilt$tryCheckFireSpreadSpeed(FireBlock instance, BlockState state, Operation<Integer> original, @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos pos, @Local Direction direction) {
        if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), IForgeBlock.class, "getFireSpreadSpeed", BlockState.class, BlockGetter.class, BlockPos.class, Direction.class)) {
            return state.getFireSpreadSpeed(level, pos.relative(direction), direction.getOpposite());
        }

        return original.call(instance, state);
    }

    @Inject(method = "setFlammable", at = @At("HEAD"))
    private void kilt$avoidSettingAirOnFire(Block block, int encouragement, int flammability, CallbackInfo ci) {
        if (block == Blocks.AIR)
            throw new IllegalArgumentException("Tried to set air on fire!");
    }

    @Override
    public boolean canCatchFire(BlockGetter level, BlockPos pos, Direction face) {
        return level.getBlockState(pos).isFlammable(level, pos, face);
    }
}