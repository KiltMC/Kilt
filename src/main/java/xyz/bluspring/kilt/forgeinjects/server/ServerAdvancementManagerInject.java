package xyz.bluspring.kilt.forgeinjects.server;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.advancements.AdvancementBuilderInjection;

@Mixin(ServerAdvancementManager.class)
public abstract class ServerAdvancementManagerInject {
    @Shadow @Final private static Logger LOGGER;
    @Unique private ICondition.IContext context = ICondition.IContext.EMPTY;

    public ServerAdvancementManagerInject(LootDataManager dataManager) {}

    @CreateInitializer
    public ServerAdvancementManagerInject(LootDataManager dataManager, ICondition.IContext context) {
        this(dataManager);
        this.context = context;
    }

    @WrapOperation(method = "method_20723", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/Advancement$Builder;fromJson(Lcom/google/gson/JsonObject;Lnet/minecraft/advancements/critereon/DeserializationContext;)Lnet/minecraft/advancements/Advancement$Builder;"))
    private Advancement.Builder kilt$tryUseContextInAdvancementLoad(JsonObject json, DeserializationContext context, Operation<Advancement.Builder> original) {
        if (this.context != ICondition.IContext.EMPTY) {
            return AdvancementBuilderInjection.fromJson(json, context, this.context);
        }

        return original.call(json, context);
    }

    @ModifyExpressionValue(method = "method_20723", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/Advancement$Builder;fromJson(Lcom/google/gson/JsonObject;Lnet/minecraft/advancements/critereon/DeserializationContext;)Lnet/minecraft/advancements/Advancement$Builder;"))
    private Advancement.Builder kilt$skipLoadingIfNull(Advancement.Builder original, @Cancellable CallbackInfo ci, @Local(argsOnly = true) ResourceLocation location) {
        if (original == null) {
            LOGGER.debug("Skipping loading advancement {} as its conditions were not met", location);
            ci.cancel();
        }

        return original;
    }
}
