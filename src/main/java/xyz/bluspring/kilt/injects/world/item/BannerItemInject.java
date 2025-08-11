package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BannerItem.class)
public abstract class BannerItemInject {
    @ModifyArg(method = "method_43707", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
    private static String kilt$useNamespacedTranslationKey(String key, @Local(argsOnly = true) DyeColor color, @Local(argsOnly = true) String fullKey) {
        var fileLoc = new ResourceLocation(fullKey);
        return "block." + fileLoc.getNamespace() + ".banner." + fileLoc.getPath() + "." + color.getName();
    }
}
