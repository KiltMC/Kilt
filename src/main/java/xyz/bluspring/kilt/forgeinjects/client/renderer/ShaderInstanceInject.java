package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.Program;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import xyz.bluspring.kilt.workarounds.ForgeHooksClientWorkaround;

@Mixin(ShaderInstance.class)
public class ShaderInstanceInject {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;<init>(Ljava/lang/String;)V"), method = "<init>")
    public String kilt$addNamespaceToResourceLocation(String string) {
        var serialized = string.replaceFirst("shaders/core/", "").replace(".json", "");
        var location = ResourceLocation.tryParse(serialized);

        if (location == null)
            return string;

        return location.getNamespace() + ":" + "shaders/core/" + location.getPath() + ".json";
    }

    @Mixin(targets = "net/minecraft/client/renderer/ShaderInstance$1")
    public static class ShaderInstanceSyntheticMixin {
        @Shadow @Final
        String val$relativePath;

        @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/FileUtil;normalizeResourcePath(Ljava/lang/String;)Ljava/lang/String;"), method = "applyImport")
        public String kilt$getForgeShaderImports(String string, boolean bl, String string2) {
            var location = ForgeHooksClientWorkaround.getShaderImportLocation(val$relativePath, bl, string2);
            return location.toString();
        }
    }
}
