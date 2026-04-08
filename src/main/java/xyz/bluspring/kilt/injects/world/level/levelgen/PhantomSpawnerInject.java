package xyz.bluspring.kilt.injects.world.level.levelgen;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.levelgen.PhantomSpawner;

@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerInject {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;blockPosition()Lnet/minecraft/core/BlockPos;"))
    private BlockPos kilt$callSpawnPhantomsEvent(BlockPos original, @Local ServerPlayer player, @Local(argsOnly = true) ServerLevel level, @Share("event") LocalRef<PlayerSpawnPhantomsEvent> eventRef) {
        eventRef.set(EventHooks.firePlayerSpawnPhantoms(player, level, original));
        return original;
    }

    @Definition(id = "dimensionType", method = "Lnet/minecraft/server/level/ServerLevel;dimensionType()Lnet/minecraft/world/level/dimension/DimensionType;")
    @Definition(id = "hasSkyLight", method = "Lnet/minecraft/world/level/dimension/DimensionType;hasSkyLight()Z")
    @Definition(id = "getY", method = "Lnet/minecraft/core/BlockPos;getY()I")
    @Definition(id = "canSeeSky", method = "Lnet/minecraft/server/level/ServerLevel;canSeeSky(Lnet/minecraft/core/BlockPos;)Z")
    @Expression(value = "?.dimensionType().hasSkyLight() == 0", id = "skylight")
    @Expression(value = "?.getY() >= ?", id = "seaLevel")
    @Expression(value = "?.canSeeSky(?)", id = "canSeeSky")
    @ModifyExpressionValue(method = "tick", at = {
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "skylight"),
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "seaLevel"),
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "canSeeSky")
    })
    private boolean kilt$checkShouldSpawnPhantoms(boolean original, @Share("event") LocalRef<PlayerSpawnPhantomsEvent> eventRef) {
        if (eventRef.get().getResult() == PlayerSpawnPhantomsEvent.Result.ALLOW) {
            return true;
        } else if (eventRef.get().getResult() == PlayerSpawnPhantomsEvent.Result.DENY) {
            return false;
        }

        return original;
    }

    @Definition(id = "difficultyInstance", local = @Local(type = DifficultyInstance.class))
    @Definition(id = "isHarderThan", method = "Lnet/minecraft/world/DifficultyInstance;isHarderThan(F)Z")
    @Definition(id = "randomSource", local = @Local(type = RandomSource.class))
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Expression(value = "difficultyInstance.isHarderThan(?)", id = "isHarderThan")
    @Expression(value = "randomSource.nextInt(?) >= ?", id = "randomCheck")
    @ModifyExpressionValue(method = "tick", at = {
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "isHarderThan"),
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "randomCheck")
    })
    private boolean kilt$checkCanForcefullySpawnPhantoms(boolean original, @Share("event") LocalRef<PlayerSpawnPhantomsEvent> eventRef) {
        return eventRef.get().getResult() == PlayerSpawnPhantomsEvent.Result.ALLOW || original;
    }

    @Definition(id = "randomSource", local = @Local(type = RandomSource.class))
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Expression("1 + randomSource.nextInt(?)")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int kilt$tryUseCustomPhantomSpawnEvent(int original, @Share("event") LocalRef<PlayerSpawnPhantomsEvent> eventRef) {
        if (eventRef.get().kilt$wasModified) {
            return eventRef.get().getPhantomsToSpawn();
        }

        return original;
    }
}
