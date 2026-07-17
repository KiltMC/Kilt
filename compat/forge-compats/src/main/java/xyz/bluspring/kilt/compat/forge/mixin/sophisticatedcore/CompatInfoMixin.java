package xyz.bluspring.kilt.compat.forge.mixin.sophisticatedcore;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.fml.ModContainer;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.loader.mod.fabric.WrappedFabricModContainer;

import java.util.Optional;

@Mixin(value = CompatInfo.class, remap = false)
public abstract class CompatInfoMixin {
    @Shadow
    public abstract String modId();

    @ModifyExpressionValue(method = "isLoaded", at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/ModList;getModContainerById(Ljava/lang/String;)Ljava/util/Optional;"))
    public Optional<? extends ModContainer> checkFabricsOpinion(Optional<? extends ModContainer> original) {
        if (original.isPresent()) {
            return original;
        }
        if (Kilt.Companion.getLoader().getNeoForgeToFabricMods().containsKey(modId()))
            return FabricLoader.getInstance().getModContainer(Kilt.Companion.getLoader().getNeoForgeToFabricMods().get(modId())).map(WrappedFabricModContainer::get);
        return FabricLoader.getInstance().getModContainer(modId().replace("_", "-")).map(WrappedFabricModContainer::get);
    }
}
