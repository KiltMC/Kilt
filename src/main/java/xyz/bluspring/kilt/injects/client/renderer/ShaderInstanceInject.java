package xyz.bluspring.kilt.injects.client.renderer;

import java.io.IOException;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.VertexFormat;
import kotlin.text.StringsKt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;

@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceInject {
    @Shadow
    @Final
    private String name;

    public ShaderInstanceInject(ResourceProvider resourceProvider, String name, VertexFormat vertexFormat) throws IOException {}

    @CreateInitializer
    public ShaderInstanceInject(ResourceProvider resourceProvider, ResourceLocation name, VertexFormat vertexFormat) throws IOException {
        this(resourceProvider, name.toString(), vertexFormat);
    }

    @Inject(method = "<init>", at = @At("HEAD"))
    private static void kilt$initShareData(ResourceProvider resourceProvider, String name, VertexFormat vertexFormat, CallbackInfo ci,
                                           @Share(value = "resourceProvider", namespace = Kilt.MOD_ID) LocalRef<ResourceProvider> resourceProviderRef,
                                           @Share(value = "vertexFormat", namespace = Kilt.MOD_ID) LocalRef<VertexFormat> vertexFormatRef,
                                           @Share(value = "shaderLocation", namespace = Kilt.MOD_ID) LocalRef<ResourceLocation> shaderLocation) {
        resourceProviderRef.set(resourceProvider);
        vertexFormatRef.set(vertexFormat);
        shaderLocation.set(ResourceLocation.tryParse(name));
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation kilt$rewriteProgramIdIfPossible(String id, Operation<ResourceLocation> original) {
        // please tell me this targets everyone correctly >:(
        if (this.name.contains(String.valueOf(ResourceLocation.NAMESPACE_SEPARATOR)) && StringsKt.count(id, c -> c == ':') == 1) {
            return FabricShaderProgram.rewriteAsId(id, this.name);
        }

        return original.call(id);
    }
}
