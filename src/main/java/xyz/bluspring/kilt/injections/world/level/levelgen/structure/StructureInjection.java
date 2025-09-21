package xyz.bluspring.kilt.injections.world.level.levelgen.structure;

import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.world.ModifiableStructureInfo;

public interface StructureInjection {
    default ModifiableStructureInfo modifiableStructureInfo() {
        throw new IllegalStateException();
    }

    default Structure.StructureSettings getModifiedStructureSettings() {
        throw new IllegalStateException();
    }
}
