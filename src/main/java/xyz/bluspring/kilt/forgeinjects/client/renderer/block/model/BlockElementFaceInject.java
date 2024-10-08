// TRACKED HASH: c651e93bacb2243f18c5b16ca365e199444ccfb3
package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.ForgeFaceData;
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
public class BlockElementFaceInject implements BlockElementFaceInjection {
    @Unique private ForgeFaceData faceData;
    @Unique private BlockElement parent;

    public BlockElementFaceInject(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv) {}
    @CreateInitializer
    public BlockElementFaceInject(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv, @Nullable ForgeFaceData faceData) {
        this(cullForDirection, tintIndex, texture, uv);
        this.faceData = faceData;
    }

    @Override
    public void kilt$setParent(BlockElement parent) {
        this.parent = parent;
    }

    @Override
    public ForgeFaceData getFaceData() {
        if (this.faceData != null)
            return this.faceData;
        else if (this.parent != null)
            return ((BlockElementInjection) this.parent).getFaceData();

        return ForgeFaceData.DEFAULT;
    }

    @Override
    public void kilt$setFaceData(ForgeFaceData faceData) {
        this.faceData = faceData;
    }

    @Mixin(BlockElementFace.Deserializer.class)
    public static class DeserializerInject {
        @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockElementFace;", at = @At("RETURN"))
        private void kilt$readForgeFaceData(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<BlockElementFace> cir, @Local JsonObject jsonObject) {
            var face = cir.getReturnValue();
            var faceData = ForgeFaceData.read(jsonObject.get("forge_data"), null);

            ((BlockElementFaceInjection) face).kilt$setFaceData(faceData);
        }
    }
}