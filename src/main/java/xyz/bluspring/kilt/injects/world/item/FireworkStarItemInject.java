package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.FireworkStarItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.item.FireworkRocketItemShapeInjection;

@Mixin(FireworkStarItem.class)
public abstract class FireworkStarItemInject {
    @WrapOperation(method = "appendHoverText(Lnet/minecraft/nbt/CompoundTag;Ljava/util/List;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/FireworkRocketItem$Shape;byId(I)Lnet/minecraft/world/item/FireworkRocketItem$Shape;"))
    private static FireworkRocketItem.Shape kilt$tryGetForgeShape(int index, Operation<FireworkRocketItem.Shape> original, @Local(argsOnly = true) CompoundTag tag) {
        var shape = FireworkRocketItemShapeInjection.getShape(tag);

        if (shape != null) {
            return shape;
        }

        // this might be useless but oh well
        return original.call(index);
    }
}
