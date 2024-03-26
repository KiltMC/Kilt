package xyz.bluspring.kilt.injections.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public interface EntityRenderDispatcherInjection {
    Map<String, EntityRenderer<? extends Player>> getSkinMap();
}
