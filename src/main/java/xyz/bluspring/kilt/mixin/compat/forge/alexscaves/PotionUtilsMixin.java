package xyz.bluspring.kilt.mixin.compat.forge.alexscaves;

import com.bawnorton.mixinsquared.TargetHandler;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(value = PotionUtils.class, priority = 1050)
public abstract class PotionUtilsMixin {
    @TargetHandler(mixin = "com.github.alexmodguy.alexscaves.mixin.PotionUtilsMixin", name = "ac_getColor", prefix = "handler")
    @ModifyArg(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"))
    private static <T> Predicate<? super T> kilt$ac$avoidNPECrash(Predicate<? super T> predicate) {
        // Due to mods such as Botania calling this method so early, there's a chance that Alex's Caves doesn't have its registry objects loaded,
        // and as such crashes early on.

        return value -> {
            try {
                return predicate.test(value);
            } catch (NullPointerException ignored) {
                return false;
            }
        };
    }
}
