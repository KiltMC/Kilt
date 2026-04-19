package xyz.bluspring.kilt.injects.world.entity.vehicle;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

@Mixin(ContainerEntity.class)
public interface ContainerEntityInject {
    @Definition(id = "Builder", type = LootParams.Builder.class)
    @Definition(id = "withParameter", method = "Lnet/minecraft/world/level/storage/loot/LootParams$Builder;withParameter(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParam;Ljava/lang/Object;)Lnet/minecraft/world/level/storage/loot/LootParams$Builder;")
    @Expression("new Builder(?).withParameter(?, ?)")
    @ModifyExpressionValue(method = "unpackChestVehicleLootTable", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LootParams.Builder kilt$tryAddAttackingParam(LootParams.Builder original) {
        if (this instanceof AbstractMinecartContainer entityContainer) {
            return original.withParameter(LootContextParams.ATTACKING_ENTITY, entityContainer);
        }

        return original;
    }
}
