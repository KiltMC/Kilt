package xyz.bluspring.kilt.injections.world.level.levelgen.structure.templatesystem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;

public interface StructureProcessorInjection {
    @Nullable
    default StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos blockPos, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings, @org.jetbrains.annotations.Nullable StructureTemplate template) {
        throw new IllegalStateException();
    }
}
