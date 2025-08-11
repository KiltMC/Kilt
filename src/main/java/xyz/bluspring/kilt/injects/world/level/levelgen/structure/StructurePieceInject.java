package xyz.bluspring.kilt.injects.world.level.levelgen.structure;

import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructurePiece.class)
public abstract class StructurePieceInject {
    // Kilt: we don't have to implement this
}
