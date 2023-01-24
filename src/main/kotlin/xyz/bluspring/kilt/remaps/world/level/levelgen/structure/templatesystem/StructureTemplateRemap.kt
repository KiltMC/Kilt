package xyz.bluspring.kilt.remaps.world.level.levelgen.structure.templatesystem

import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.phys.Vec3

open class StructureTemplateRemap : StructureTemplate() {
    companion object {
        @JvmStatic
        fun processBlockInfos(
            level: LevelAccessor,
            pos: BlockPos,
            pos2: BlockPos,
            settings: StructurePlaceSettings,
            infos: List<StructureTemplate.StructureBlockInfo>,
            template: StructureTemplate?
        ): List<StructureTemplate.StructureBlockInfo> {
            // TODO: Should probably use the structure template system.
            return StructureTemplate.processBlockInfos(level, pos, pos2, settings, infos)
        }

        // TODO: This is still unfinished, need to add processEntityInfos over.
    }
}