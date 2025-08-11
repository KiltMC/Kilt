package xyz.bluspring.kilt.injects.world.entity.npc;

import net.minecraft.world.entity.npc.CatSpawner;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CatSpawner.class)
public abstract class CatSpawnerInject {
    // Kilt: this is a bugfix inject, not needed for us.
}
