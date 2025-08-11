package xyz.bluspring.kilt.injects.world.level.levelgen.structure;

import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureStart.class)
public abstract class StructureStartInject {
    // Kilt: we don't have to implement this
}
