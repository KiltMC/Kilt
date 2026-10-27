package xyz.bluspring.kilt.injects.client.resources.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.resources.model.SimpleModelWrapperInjection;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.resources.Identifier;

@Mixin(SimpleModelWrapper.class)
public abstract class SimpleModelWrapperInject {
    @CreateStatic
    private static BlockStateModelPart bake(ModelBaker modelBakery, ResolvedModel model, ModelState state) {
        return SimpleModelWrapperInjection.bake(modelBakery, model, state);
    }

    @WrapOperation(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBaker;getModel(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/resources/model/ResolvedModel;"))
    private static ResolvedModel kilt$tryUseStoredResolvedModel(ModelBaker instance, Identifier identifier, Operation<ResolvedModel> original) {
        var model = SimpleModelWrapperInjection.kilt$model.get();
        if (model != null)
            return model;

        return original.call(instance, identifier);
    }
}
