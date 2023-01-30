package xyz.bluspring.kilt.forgeinjects.world.entity;

import net.minecraft.world.entity.SpawnPlacements;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.entity.SpawnPlacementsInjection;

@Mixin(SpawnPlacements.class)
public class SpawnPlacementsInject implements SpawnPlacementsInjection {
}
