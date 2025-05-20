package xyz.bluspring.kilt.forgeinjects.world.entity.vehicle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ContainerEntity.class)
public interface ContainerEntityInject {
    @ModifyExpressionValue(method = "unpackChestVehicleLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootParams$Builder;withParameter(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParam;Ljava/lang/Object;)Lnet/minecraft/world/level/storage/loot/LootParams$Builder;"))
    default LootParams.Builder kilt$addKillerEntityParam(LootParams.Builder original) {
        if (this instanceof AbstractMinecartContainer container)
            return original.withParameter(LootContextParams.KILLER_ENTITY, container);

        return original;
    }
}
