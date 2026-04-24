package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.FarmlandWaterManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

@Mixin(value = FarmBlock.class, priority = 1050)
public abstract class FarmBlockInject extends Block {
    public FarmBlockInject(Properties properties) {
        super(properties);
    }

    @Definition(id = "level", local = @Local(type = Level.class, argsOnly = true))
    @Definition(id = "random", field = "Lnet/minecraft/world/level/Level;random:Lnet/minecraft/util/RandomSource;")
    @Definition(id = "nextFloat", method = "Lnet/minecraft/util/RandomSource;nextFloat()F")
    @Definition(id = "fallDistance", local = @Local(type = float.class, argsOnly = true))
    @Definition(id = "entity", local = @Local(type = Entity.class, argsOnly = true))
    @Definition(id = "LivingEntity", type = LivingEntity.class)
    @Definition(id = "Player", type = Player.class)
    @Definition(id = "getGameRules", method = "Lnet/minecraft/world/level/Level;getGameRules()Lnet/minecraft/world/level/GameRules;")
    @Definition(id = "getBoolean", method = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z")
    @Definition(id = "RULE_MOBGRIEFING", field = "Lnet/minecraft/world/level/GameRules;RULE_MOBGRIEFING:Lnet/minecraft/world/level/GameRules$Key;")
    @Definition(id = "getBbWidth", method = "Lnet/minecraft/world/entity/Entity;getBbWidth()F")
    @Definition(id = "getBbHeight", method = "Lnet/minecraft/world/entity/Entity;getBbHeight()F")
    @Expression(value = "level.random.nextFloat() < fallDistance - 0.5", id = "random")
    @Expression(value = "entity instanceof LivingEntity", id = "entity")
    @Expression(value = "entity instanceof Player", id = "player")
    @Expression(value = "level.getGameRules().getBoolean(RULE_MOBGRIEFING)", id = "mobGriefing")
    @Expression(value = "entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512", id = "hitbox")
    @ModifyExpressionValue(method = "fallOn", at = {
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "random"),
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "entity"),
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "player"),
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "mobGriefing"),
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "hitbox")
    })
    private boolean kilt$runForgeTurnToDirtCheck(boolean original, @Local(argsOnly = true) Entity entity) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(
            entity.getClass(), Entity.class, "canTrample", boolean.class,
            BlockState.class, BlockPos.class, float.class
        )) {
            return true;
        }

        return original;
    }

    @WrapWithCondition(method = "fallOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FarmBlock;turnToDirt(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private boolean kilt$handleFarmlandTrample(Entity entity, BlockState state, Level level, BlockPos pos, @Local(argsOnly = true) float fallDistance) {
        return CommonHooks.onFarmlandTrample(level, pos, Blocks.DIRT.defaultBlockState(), fallDistance, entity);
    }

    @WrapOperation(method = "isNearWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean kilt$checkCanBeHydrated(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original, @Local(argsOnly = true) LevelReader levelReader, @Local(argsOnly = true) BlockPos pos, @Local(ordinal = 1) BlockPos blockPos) {
        var state = levelReader.getBlockState(pos);
        return original.call(instance, tag) || state.canBeHydrated(levelReader, pos, instance, blockPos);
    }

    @ModifyReturnValue(method = "isNearWater", at = @At(value = "RETURN", ordinal = 1))
    private static boolean kilt$checkHasBlockWaterTicket(boolean original, @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos pos) {
        return FarmlandWaterManager.hasBlockWaterTicket(level, pos);
    }
}
