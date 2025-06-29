package xyz.bluspring.kilt.forgeinjects.world.level.levelgen;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerInject {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;hasSkyLight()Z", ordinal = 1))
    private boolean kilt$callPhantomSpawnEvent(DimensionType instance, Operation<Boolean> original, @Share("event") LocalRef<PlayerSpawnPhantomsEvent> eventRef, @Share("shouldCancel") LocalBooleanRef shouldCancel, @Local(argsOnly = true) ServerLevel level, @Local(ordinal = 0) BlockPos pos, @Local RandomSource random, @Local ServerPlayer player) {
        var difficulty = level.getCurrentDifficultyAt(pos);
        var event = new PlayerSpawnPhantomsEvent(player, 1 + random.nextInt(difficulty.getDifficulty().getId() + 1));
        MinecraftForge.EVENT_BUS.post(event);
        eventRef.set(event);

        if (event.getResult() == Event.Result.DENY) {
            shouldCancel.set(true);
            return true;
        }

        return event.getResult() == Event.Result.ALLOW || original.call(instance);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;canSeeSky(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$cancelIfRequired(ServerLevel instance, BlockPos blockPos, Operation<Boolean> original, @Share("shouldCancel") LocalBooleanRef shouldCancel) {
        return original.call(instance, blockPos) && !shouldCancel.get();
    }

    // MixinExtras my beloved
    @Definition(id = "randomSource", local = @Local(type = RandomSource.class))
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Expression("randomSource.nextInt(?) >= 72000")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkAllowEvent(boolean original, @Share("event") LocalRef<PlayerSpawnPhantomsEvent> eventRef) {
        return eventRef.get().getResult() == Event.Result.ALLOW || original;
    }

    @Definition(id = "randomSource", local = @Local(type = RandomSource.class))
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Definition(id = "difficultyInstance", local = @Local(type = DifficultyInstance.class))
    @Definition(id = "getDifficulty", method = "Lnet/minecraft/world/DifficultyInstance;getDifficulty()Lnet/minecraft/world/Difficulty;")
    @Definition(id = "getId", method = "Lnet/minecraft/world/Difficulty;getId()I")
    @Expression("1 + randomSource.nextInt(difficultyInstance.getDifficulty().getId() + 1)")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int kilt$usePhantomsToSpawnCheck(int original, @Share("event") LocalRef<PlayerSpawnPhantomsEvent> eventRef) {
        if (original != eventRef.get().kilt$getOldPhantomsToSpawn())
            return original;

        return eventRef.get().getPhantomsToSpawn();
    }
}
