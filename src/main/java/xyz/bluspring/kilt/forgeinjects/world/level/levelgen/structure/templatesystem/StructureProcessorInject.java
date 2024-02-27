// TRACKED HASH: 1c71a68099025e02b23495d36736a4a8d42f2db3
package xyz.bluspring.kilt.forgeinjects.world.level.levelgen.structure.templatesystem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.AbstractOverride;
import xyz.bluspring.kilt.injections.world.level.levelgen.structure.templatesystem.StructureProcessorInjection;

@Mixin(StructureProcessor.class)
public abstract class StructureProcessorInject implements StructureProcessorInjection {
    @AbstractOverride
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos blockPos, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings) {
        return relativeBlockInfo;
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos blockPos, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings, @Nullable StructureTemplate template) {
        return processBlock(level, blockPos, pos, blockInfo, relativeBlockInfo, settings);
    }
}