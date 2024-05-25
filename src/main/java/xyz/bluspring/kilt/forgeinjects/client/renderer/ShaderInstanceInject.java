// TRACKED HASH: 536471959d94fe44f630b2fc9adf3b6aafdf436a
package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.bawnorton.mixinsquared.TargetHandler;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import java.io.IOException;

@Mixin(value = ShaderInstance.class, priority = 1050)
public abstract class ShaderInstanceInject {
    // Handled by Fabric API
    /*@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;<init>(Ljava/lang/String;)V"), method = "<init>")
    public String kilt$addNamespaceToResourceLocation(String string) {
        var serialized = string.replaceFirst("shaders/core/", "").replace(".json", "");
        var location = ResourceLocation.tryParse(serialized);

        if (location == null)
            return string;

        return location.getNamespace() + ":" + "shaders/core/" + location.getPath() + ".json";
    }*/

    @Shadow @Final private String name;

    @Mixin(targets = "net/minecraft/client/renderer/ShaderInstance$1")
    public static class ShaderInstanceSyntheticMixin {
        @Shadow @Final
        String val$relativePath;

        @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/FileUtil;normalizeResourcePath(Ljava/lang/String;)Ljava/lang/String;"), method = "applyImport")
        public String kilt$getForgeShaderImports(String string, boolean bl, String string2) {
            var location = ForgeHooksClient.getShaderImportLocation(val$relativePath, bl, string2);
            return location.toString();
        }
    }

    public ShaderInstanceInject(ResourceProvider resourceProvider, String string, VertexFormat vertexFormat) throws IOException {}

    @CreateInitializer
    public ShaderInstanceInject(ResourceProvider provider, ResourceLocation resourceLocation, VertexFormat vertexFormat) throws IOException {
        this(provider, resourceLocation.toString(), vertexFormat);
    }

    @SuppressWarnings({"InvalidMemberReference", "MixinAnnotationTarget"})
    @TargetHandler(
        mixin = "net.fabricmc.fabric.mixin.client.rendering.shader.ShaderProgramMixin",
        name = "modifyProgramId"
    )
    @Inject(
        method = "@MixinSquared:Handler",
        at = @At("HEAD"),
        cancellable = true
    )
    private void kilt$addForgeSupportToFabricAPI(String id, CallbackInfoReturnable<String> cir) {
        if (id.contains(":")) {
            cir.setReturnValue(FabricShaderProgram.rewriteAsId(id, this.name));
        }
    }
}