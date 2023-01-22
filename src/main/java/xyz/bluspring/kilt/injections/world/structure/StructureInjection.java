package xyz.bluspring.kilt.injections.world.structure;

import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.world.ModifiableStructureInfo;

public interface StructureInjection {
    default ModifiableStructureInfo modifiableStructureInfo() {
        throw new IllegalStateException();
    }

    default Structure.StructureSettings getModifiedStructureSettings() {
        throw new IllegalStateException();
    }
}
