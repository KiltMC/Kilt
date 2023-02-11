package xyz.bluspring.kilt.mixin.fabric;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.level.DataPackConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.kilt.resources.KiltResourceLoader;

import java.util.List;

@Mixin(value = ModResourcePackUtil.class, remap = false)
public class ModResourcePackUtilMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/resource/loader/ModResourcePackCreator;register(Ljava/util/function/Consumer;)V", remap = false), method = "createDefaultDataPackSettings", locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private static void kilt$appendKiltResources(CallbackInfoReturnable<DataPackConfig> cir, ModResourcePackCreator modResourcePackCreator, List<Pack> moddedResourcePacks) {
        KiltResourceLoader.loadResources(PackType.SERVER_DATA, moddedResourcePacks::add, null);
    }
}
