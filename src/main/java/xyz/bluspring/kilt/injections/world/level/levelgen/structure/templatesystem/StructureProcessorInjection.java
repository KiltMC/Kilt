package xyz.bluspring.kilt.injections.world.level.levelgen.structure.templatesystem;

import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public interface StructureProcessorInjection {
    @Nullable
    default StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos blockPos, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings, @org.jetbrains.annotations.Nullable StructureTemplate template) {
        throw KiltHelper.createMixinException(StructureProcessorInjection.class, "process");
    }

    default StructureTemplate.StructureEntityInfo processEntity(LevelReader world, BlockPos seedPos, StructureTemplate.StructureEntityInfo rawEntityInfo, StructureTemplate.StructureEntityInfo entityInfo, StructurePlaceSettings placementSettings, StructureTemplate template) {
        throw KiltHelper.createMixinException(StructureProcessorInjection.class, "processEntity");
    }
}
