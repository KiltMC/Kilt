package xyz.bluspring.kilt.injections.client.resources.model;

import xyz.bluspring.kilt.Kilt;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;

public interface SimpleModelWrapperInjection {
    ThreadLocal<ResolvedModel> kilt$model = new ThreadLocal<>();

    static BlockStateModelPart bake(ModelBaker modelBakery, ResolvedModel model, ModelState state) {
        try {
            kilt$model.set(model);
            return SimpleModelWrapper.bake(modelBakery, Kilt.id("neoforge/resolved_model_bake"), state);
        } finally {
            kilt$model.set(null);
        }
    }
}
