// TRACKED HASH: 7806cbd7ecf0842aa5db2c08ecd295f2b0b0f3ed
package xyz.bluspring.kilt.forgeinjects.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.renderer.entity.EntityRenderDispatcherInjection;

import java.util.Collections;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherInject implements EntityRenderDispatcherInjection {

    @Shadow private Map<String, EntityRenderer<? extends Player>> playerRenderers;

    @Override
    public Map<String, EntityRenderer<? extends Player>> getSkinMap() {
        return Collections.unmodifiableMap(this.playerRenderers);
    }
}