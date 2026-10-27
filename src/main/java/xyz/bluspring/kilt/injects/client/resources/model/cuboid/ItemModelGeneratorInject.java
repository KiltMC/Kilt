package xyz.bluspring.kilt.injects.client.resources.model.cuboid;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Transparency;
import com.mojang.math.Quadrant;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.resources.model.cuboid.FaceBakeryInjection;
import xyz.bluspring.kilt.injections.client.resources.model.cuboid.ItemModelGeneratorInjection;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.util.context.ContextMap;

@Mixin(ItemModelGenerator.class)
public abstract class ItemModelGeneratorInject implements ItemModelGeneratorInjection {
    @Shadow
    private static QuadCollection bake(TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    public static void bakeSideFaces(QuadCollection.Builder builder, ModelBaker.Interner interner, ModelState modelState, BakedQuad.MaterialInfo materialInfo) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Unique private static final ThreadLocal<ContextMap> kilt$additionalProperties = new ThreadLocal<>();

    @Unique
    private static QuadCollection bake(TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name, ContextMap additionalProperties) {
        try {
            kilt$additionalProperties.set(additionalProperties);
            return bake(textureSlots, modelBaker, modelState, name);
        } finally {
            kilt$additionalProperties.set(null);
        }
    }

    @CreateStatic
    private static void bakeSideFaces(QuadCollection.Builder builder, ModelBaker.Interner interner, ModelState modelState, BakedQuad.MaterialInfo materialInfo, ExtraFaceData faceData) {
        try {
            kilt$faceData.get().push(faceData);
            bakeSideFaces(builder, interner, modelState, materialInfo);
        } finally {
            kilt$faceData.get().pop();
        }
    }

    @Definition(id = "ItemLayerKey", type = ItemModelGenerator.ItemLayerKey.class)
    @Expression("new ItemLayerKey(?, ?, ?)")
    @WrapOperation(method = "bake", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static ItemModelGenerator.ItemLayerKey kilt$tryAttachFaceData(Material.Baked material, ModelState modelState, int layerIndex, Operation<ItemModelGenerator.ItemLayerKey> original) {
        var faceData = kilt$additionalProperties.get() != null ? NeoForgeModelProperties.getItemLayerFaceData(kilt$additionalProperties.get(), layerIndex) : ExtraFaceData.DEFAULT;
        var key = original.call(material, modelState, layerIndex);
        ClientRecordMixinRefMaps.EXTENDED_ITEM_LAYER_KEY.put(key, new ClientRecordMixinRefMaps.ExtendedItemLayerKey(faceData));
        return key;
    }

    @WrapOperation(method = "bakeExtrudedSprite", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/cuboid/FaceBakery;bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker$Interner;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/resources/model/cuboid/CuboidFace$UVs;Lcom/mojang/math/Quadrant;Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;)Lnet/minecraft/client/resources/model/geometry/BakedQuad;"))
    private static BakedQuad kilt$tryBakeSpriteWithFaceData(ModelBaker.Interner interner, Vector3fc from, Vector3fc _to, CuboidFace.UVs uvs, Quadrant uvRotation, BakedQuad.MaterialInfo materialInfo, Direction facing, ModelState modelState, CuboidRotation elementRotation, Operation<BakedQuad> original) {
        try {
            FaceBakeryInjection.kilt$faceData.get().push(kilt$faceData.get().top());
            return original.call(interner, from, _to, uvs, uvRotation, materialInfo, facing, modelState, elementRotation);
        } finally {
            FaceBakeryInjection.kilt$faceData.get().pop();
        }
    }

    @WrapOperation(method = "bakeSideFaces", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/cuboid/FaceBakery;bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker$Interner;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/resources/model/cuboid/CuboidFace$UVs;Lcom/mojang/math/Quadrant;Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;)Lnet/minecraft/client/resources/model/geometry/BakedQuad;"))
    private static BakedQuad kilt$tryBakeSideFacesWithFaceData(ModelBaker.Interner interner, Vector3fc from, Vector3fc _to, CuboidFace.UVs uvs, Quadrant uvRotation, BakedQuad.MaterialInfo materialInfo, Direction facing, ModelState modelState, CuboidRotation elementRotation, Operation<BakedQuad> original) {
        try {
            FaceBakeryInjection.kilt$faceData.get().push(kilt$faceData.get().top());
            return original.call(interner, from, _to, uvs, uvRotation, materialInfo, facing, modelState, elementRotation);
        } finally {
            FaceBakeryInjection.kilt$faceData.get().pop();
        }
    }

    @Mixin(ItemModelGenerator.ItemLayerKey.class)
    public abstract static class ItemLayerKeyInject implements ItemLayerKeyInjection {
        public ItemLayerKeyInject(Material.Baked material, ModelState modelState, int layerIndex) {}

        @CreateInitializer
        public ItemLayerKeyInject(Material.Baked material, ModelState modelState, int layerIndex, ExtraFaceData faceData) {
            this(material, modelState, layerIndex);
            ClientRecordMixinRefMaps.EXTENDED_ITEM_LAYER_KEY.put((ItemModelGenerator.ItemLayerKey) (Object) this, new ClientRecordMixinRefMaps.ExtendedItemLayerKey(faceData));
        }

        @Override
        public ExtraFaceData faceData() {
            if (ClientRecordMixinRefMaps.EXTENDED_ITEM_LAYER_KEY.containsKey(this)) {
                return ClientRecordMixinRefMaps.EXTENDED_ITEM_LAYER_KEY.get(this).faceData();
            }

            return ExtraFaceData.DEFAULT;
        }

        @WrapOperation(method = "compute(Lnet/minecraft/client/resources/model/ModelBaker;)Lnet/minecraft/client/resources/model/geometry/QuadCollection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;of(Lnet/minecraft/client/resources/model/sprite/Material$Baked;Lcom/mojang/blaze3d/platform/Transparency;IZI)Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;"))
        private BakedQuad.MaterialInfo kilt$attachFaceDataToMaterialInfo(Material.Baked material, Transparency transparency, int tintIndex, boolean shade, int lightEmission, Operation<BakedQuad.MaterialInfo> original) {
            var faceData = this.faceData();
            if (faceData != ExtraFaceData.DEFAULT) {
                lightEmission = faceData.lightEmission();
            }

            var info = original.call(material, transparency, tintIndex, shade, lightEmission);
            if (faceData != ExtraFaceData.DEFAULT) {
                ClientRecordMixinRefMaps.EXTENDED_BAKED_QUAD_MATERIAL_INFO.put(info, new ClientRecordMixinRefMaps.ExtendedBakedQuadMaterialInfo(faceData.ambientOcclusion()));
            }

            return info;
        }

        @WrapOperation(method = "compute(Lnet/minecraft/client/resources/model/ModelBaker;)Lnet/minecraft/client/resources/model/geometry/QuadCollection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/cuboid/ItemModelGenerator;bakeExtrudedSprite(Lnet/minecraft/client/resources/model/geometry/QuadCollection$Builder;Lnet/minecraft/client/resources/model/ModelBaker$Interner;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;)V"))
        private void kilt$attachFaceDataToInfo(QuadCollection.Builder builder, ModelBaker.Interner interner, ModelState modelState, BakedQuad.MaterialInfo materialInfo, Operation<Void> original) {
            try {
                ItemModelGeneratorInjection.kilt$faceData.get().push(this.faceData());
                original.call(builder, interner, modelState, materialInfo);
            } finally {
                ItemModelGeneratorInjection.kilt$faceData.get().pop();
            }
        }
    }
}
