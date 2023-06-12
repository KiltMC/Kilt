package xyz.bluspring.kilt.forgeinjects.network.syncher;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityDataSerializers.class)
public class EntityDataSerializersInject {
    @Shadow @Final private static CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS;

    @Inject(method = "getSerializer", at = @At("HEAD"), cancellable = true)
    private static void kilt$getSerializerFromForge(int id, CallbackInfoReturnable<EntityDataSerializer<?>> cir) {
        cir.setReturnValue(ForgeHooks.getSerializer(id, SERIALIZERS));
    }

    @Inject(method = "getSerializedId", at = @At("HEAD"), cancellable = true)
    private static void kilt$getSerializerIdFromForge(EntityDataSerializer<?> serializer, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(ForgeHooks.getSerializerId(serializer, SERIALIZERS));
    }
}
