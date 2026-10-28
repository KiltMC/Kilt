package xyz.bluspring.kilt.mixin.workarounds.what_the_fuck;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.resources.ResourceLocation;

@Mixin(AdvancementTree.class)
public abstract class AdvancementTreeMixin {
    @ModifyExpressionValue(method = "tryInsert", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
    private boolean kilt$whatTheFuck(boolean original, @Local Optional<ResourceLocation> parent, @Local(argsOnly = true) AdvancementHolder advancement) {
        // Otherwise, recipe advancements just do not load, because minecraft:recipes/root depends on minecraft:recipes/root somehow.
        // I do not know why.
        // Don't ask me why, this just works for now.
        return original && !parent.orElseThrow().equals(advancement.id());
    }
}
