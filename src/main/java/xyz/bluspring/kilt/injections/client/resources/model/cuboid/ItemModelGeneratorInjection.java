package xyz.bluspring.kilt.injections.client.resources.model.cuboid;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;

public interface ItemModelGeneratorInjection {
    ThreadLocal<Stack<ExtraFaceData>> kilt$faceData = ThreadLocal.withInitial(() -> {
        var stack = new ObjectArrayList<ExtraFaceData>();
        stack.push(ExtraFaceData.DEFAULT);
        return stack;
    });

    static void bakeSideFaces(QuadCollection.Builder builder, ModelBaker.Interner interner, ModelState modelState, BakedQuad.MaterialInfo materialInfo, ExtraFaceData faceData) {
        try {
            kilt$faceData.get().push(faceData);
            ItemModelGenerator.bakeSideFaces(builder, interner, modelState, materialInfo);
        } finally {
            kilt$faceData.get().pop();
        }
    }

    interface ItemLayerKeyInjection {
        default ExtraFaceData faceData() {
            throw KiltHelper.createMixinException(ItemLayerKeyInjection.class, "faceData");
        }

        static ItemModelGenerator.ItemLayerKey create(Material.Baked material, ModelState modelState, int layerIndex, ExtraFaceData faceData) {
            var key = new ItemModelGenerator.ItemLayerKey(material, modelState, layerIndex);
            ClientRecordMixinRefMaps.EXTENDED_ITEM_LAYER_KEY.put(key, new ClientRecordMixinRefMaps.ExtendedItemLayerKey(faceData));
            return key;
        }
    }
}
