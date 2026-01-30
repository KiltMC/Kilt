// TRACKED HASH: c651e93bacb2243f18c5b16ca365e199444ccfb3
package xyz.bluspring.kilt.injects.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockElementFaceInjection;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockElementInjection;

import java.lang.reflect.Type;

@Mixin(BlockElementFace.class)
public abstract class BlockElementFaceInject implements BlockElementFaceInjection {
    @Unique private ExtraFaceData faceData;
    @Unique private MutableObject<BlockElement> parent = new MutableObject<>();

    public BlockElementFaceInject(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv) {}

    @CreateInitializer
    public BlockElementFaceInject(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv, @Nullable ExtraFaceData faceData, MutableObject<BlockElement> parent) {
        this(cullForDirection, tintIndex, texture, uv);
        this.faceData = faceData;
        this.parent = parent;
    }

    @Override
    public void kilt$setParent(MutableObject<BlockElement> parent) {
        this.parent = parent;
    }

    @Override
    public ExtraFaceData faceData() {
        if (this.faceData != null)
            return this.faceData;
        else if (this.parent != null)
            return this.parent.getValue().getFaceData();

        return ExtraFaceData.DEFAULT;
    }

    @Override
    public void kilt$setFaceData(ExtraFaceData faceData) {
        this.faceData = faceData;
    }

    @Mixin(BlockElementFace.Deserializer.class)
    public static class DeserializerInject {
        @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockElementFace;", at = @At("RETURN"))
        private void kilt$readForgeFaceData(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<BlockElementFace> cir, @Local JsonObject jsonObject) {
            var face = cir.getReturnValue();
            var faceData = ExtraFaceData.read(jsonObject.get("neoforge_data"), null);

            face.kilt$setFaceData(faceData);
        }
    }
}