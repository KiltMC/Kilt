package xyz.bluspring.kilt.forgeinjects.world.level.levelgen.feature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraftforge.common.DungeonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MonsterRoomFeature.class)
public abstract class MonsterRoomFeatureInject {
    @ModifyReturnValue(method = "randomEntityId", at = @At("RETURN"))
    private EntityType<?> kilt$tryReturnRandomDungeonMob(EntityType<?> original, @Local(argsOnly = true) RandomSource randomSource) {
        return DungeonHooks.getRandomDungeonMob(randomSource);
    }
}
