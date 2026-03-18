package xyz.bluspring.kilt.injects.client.renderer.texture.atlas;

import net.neoforged.neoforge.client.textures.SpriteContentsConstructor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

@Mixin(SpriteResourceLoader.class)
public interface SpriteResourceLoaderInject {
    @Shadow @Nullable SpriteContents loadSprite(ResourceLocation resourceLocation, Resource resource);

    // Kilt TODO: how in the fuck do we do this one?

    @Nullable
    default SpriteContents loadSprite(ResourceLocation id, Resource resource, SpriteContentsConstructor constructor) {
        return loadSprite(id, resource);
    }
}
