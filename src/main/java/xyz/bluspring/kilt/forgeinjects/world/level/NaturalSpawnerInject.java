package xyz.bluspring.kilt.forgeinjects.world.level;

import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.level.NaturalSpawnerInjection;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerInject implements NaturalSpawnerInjection {
}
