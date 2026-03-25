// TRACKED HASH: 381478d70082864904d99c0e2af6d7b72e1615b7
package xyz.bluspring.kilt.forgeinjects.network.syncher;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityDataSerializers.class)
public class EntityDataSerializersInject {
    @ModifyReturnValue(method = "getSerializer", at = @At("RETURN"))
    private static EntityDataSerializer<?> kilt$getSerializerFromForge(EntityDataSerializer<?> original, @Local(argsOnly = true) int id) {
        if (original == null) {
            return ForgeHooks.kilt$getSerializer(id);
        }

        return original;
    }

    @ModifyReturnValue(method = "getSerializedId", at = @At("RETURN"))
    private static int kilt$getSerializerIdFromForge(int original, @Local(argsOnly = true) EntityDataSerializer<?> serializer) {
        if (original == -1) {
            return ForgeHooks.kilt$getSerializerId(serializer);
        }

        return original;
    }
}