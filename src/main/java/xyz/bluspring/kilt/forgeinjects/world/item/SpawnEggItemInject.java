// TRACKED HASH: 47476edb59fcfcf846692ab9ec3986cad3c5d6c4
package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.world.item.SpawnEggItemInjection;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemInject implements SpawnEggItemInjection {
    @Shadow @Final private EntityType<?> defaultType;

    @Redirect(method = "getType", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/SpawnEggItem;defaultType:Lnet/minecraft/world/entity/EntityType;", opcode = 0))
    private EntityType<?> kilt$useForgeDefaultType(SpawnEggItem instance) {
        return this.getDefaultType();
    }

    @ModifyReturnValue(method = "getType", at = @At("RETURN"))
    private EntityType<?> kilt$returnForgeDefaultType(EntityType<?> original) {
        return this.getDefaultType();
    }

    @ModifyReceiver(method = "requiredFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;requiredFeatures()Lnet/minecraft/world/flag/FeatureFlagSet;"))
    private EntityType<?> kilt$useForgeDefaultTypeForFeatures(EntityType<?> instance) {
        return this.getDefaultType();
    }

    @Override
    public EntityType<?> getDefaultType() {
        return this.defaultType;
    }
}