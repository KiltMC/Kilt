package xyz.bluspring.kilt.injects.client.renderer.texture.atlas;

import com.google.common.collect.HashBiMap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.texture.atlas.SpriteSources;

@Mixin(SpriteSources.class)
public abstract class SpriteSourcesInject {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/HashBiMap;create()Lcom/google/common/collect/HashBiMap;"))
    private static <K, V> HashBiMap<K, V> kilt$makeSpriteSourceTypes(HashBiMap<K, V> original) {
        return (HashBiMap<K, V>) ClientHooks.makeSpriteSourceTypesMap();
    }
}
