package xyz.bluspring.kilt.injects.server;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.workarounds.ContextAwareReloadListenerWorkaround;

import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;

@Mixin(ServerAdvancementManager.class)
public abstract class ServerAdvancementManagerInject extends SimpleJsonResourceReloadListener implements ContextAwareReloadListenerWorkaround {
    @Shadow @Final private static Logger LOGGER;

    public ServerAdvancementManagerInject(Gson gson, String directory) {
        super(gson, directory);
    }

    // Kilt: handled by Fabric API
    /*@ModifyExpressionValue(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/HolderLookup$Provider;createSerializationContext(Lcom/mojang/serialization/DynamicOps;)Lnet/minecraft/resources/RegistryOps;"))
    private <V> RegistryOps<V> kilt$tryMakeConditionalContext(RegistryOps<V> original) {
        return this.kilt$makeConditionalOps(original);
    }

    @WrapOperation(method = "method_20723", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
    private <A, R> DataResult<A> kilt$tryUseContextInAdvancementLoad(Codec<A> instance, DynamicOps<R> dynamicOps, R o, Operation<DataResult<A>> original, @Cancellable CallbackInfo ci, @Local(argsOnly = true) ResourceLocation location) {
        if (this.kilt$asContextAware().getContext() != ICondition.IContext.EMPTY) {
            // Kilt TODO: I suspect this can be improved somehow...
            Codec<Optional<WithConditions<A>>> conditionalCodec = ConditionalOps.createConditionalCodecWithConditions(instance);

            Optional<A> advancement = ICondition.getWithWithConditionsCodec(conditionalCodec, dynamicOps, o);

            if (advancement.isEmpty()) {
                LOGGER.debug("Skipping loading advancement {} as its conditions were not met", location);
                ci.cancel();
                return DataResult.error(() -> "Kilt: You should not be able to see this!");
            }

            return DataResult.success(advancement.orElseThrow());
        } else {
            return original.call(instance, dynamicOps, o);
        }
    }*/
}
