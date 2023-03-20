package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.mojang.blaze3d.shaders.Program;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShaderInstance.class)
public class ShaderInstanceInject {
    @Redirect(at = @At(value = "NEW", target = "Lnet/minecraft/resources/ResourceLocation;<init>(Ljava/lang/String;)V"), method = "<init>")
    public ResourceLocation kilt$addNamespaceToResourceLocation(String string, ResourceProvider provider, String serialized) {
        var location = ResourceLocation.tryParse(serialized);
        if (location == null)
            return new ResourceLocation(string);

        return new ResourceLocation(location.getNamespace(), "shaders/core/" + location.getPath() + ".json");
    }

    @ModifyVariable(method = "getOrCreate", at = @At("STORE"), ordinal = 0, argsOnly = true)
    private static String kilt$useResourceLocation(String value, ResourceProvider provider, Program.Type type, String string) {
        var location = ResourceLocation.tryParse(string);

        if (location == null)
            return value;

        return location.getNamespace() + ":" + "shaders/core/" + location.getPath() + type.getExtension();
    }

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
}
