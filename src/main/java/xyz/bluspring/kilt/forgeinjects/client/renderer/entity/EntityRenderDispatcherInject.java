// TRACKED HASH: 7806cbd7ecf0842aa5db2c08ecd295f2b0b0f3ed
package xyz.bluspring.kilt.forgeinjects.client.renderer.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.entity.EntityRenderDispatcherInjection;

import java.util.Collections;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherInject implements EntityRenderDispatcherInjection {

    @Shadow private Map<String, EntityRenderer<? extends Player>> playerRenderers;

    @Shadow public Map<EntityType<?>, EntityRenderer<?>> renderers;

    @Override
    public Map<String, EntityRenderer<? extends Player>> getSkinMap() {
        return Collections.unmodifiableMap(this.playerRenderers);
    }

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void kilt$addEntityRenderLayers(ResourceManager resourceManager, CallbackInfo ci, @Local EntityRendererProvider.Context context) {
        ModLoader.get().postEvent(new EntityRenderersEvent.AddLayers(this.renderers, this.playerRenderers, context));
    }
}