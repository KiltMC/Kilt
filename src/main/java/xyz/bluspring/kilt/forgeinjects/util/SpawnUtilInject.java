package xyz.bluspring.kilt.forgeinjects.util;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpawnUtil.class)
public abstract class SpawnUtilInject {
    @WrapOperation(method = "trySpawnMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnObstruction(Lnet/minecraft/world/level/LevelReader;)Z"))
    private static boolean kilt$checkForgeSpawnPosition(Mob instance, LevelReader level, Operation<Boolean> original, @Local(argsOnly = true) ServerLevel serverLevel, @Local(argsOnly = true) MobSpawnType spawnType) {
        var value = ForgeEventFactory.checkSpawnPosition(instance, serverLevel, spawnType);

        if (ForgeEventFactory.kilt$isDefault.getAndSet(false)) {
            return original.call(instance, level);
        }

        return value;
    }
}
