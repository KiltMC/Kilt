package xyz.bluspring.kilt.injects.client.renderer;

import com.mojang.blaze3d.shaders.Program;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

@Mixin(EffectInstance.class)
public abstract class EffectInstanceInject {
    @Redirect(
        method = "<init>",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;",
            ordinal = 0
        ), require = 0
    )
    private ResourceLocation mojangPls(String _0, ResourceProvider rm, String str) {
        return mojangPls(ResourceLocation.parse(str), ".json");
    }

    @Redirect(
        method = "getOrCreate",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;",
            ordinal = 0
        ), require = 0
    )
    private static ResourceLocation mojangPls(String _0, ResourceProvider rm, Program.Type type, String str) {
        return mojangPls(ResourceLocation.parse(str), type.getExtension());
    }

    @Unique
    private static ResourceLocation mojangPls(ResourceLocation rl, String ext) {
        return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), "shaders/program/" + rl.getPath() + ext);
    }
}
