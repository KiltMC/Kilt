package xyz.bluspring.kilt.injections.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public interface StructureTemplateInjection {
    static Vec3 transformedVec3d(StructurePlaceSettings placementIn, Vec3 pos) {
        return StructureTemplate.transform(pos, placementIn.getMirror(), placementIn.getRotation(), placementIn.getRotationPivot());
    }

    static List<StructureTemplate.StructureEntityInfo> processEntityInfos(@Nullable StructureTemplate template, LevelAccessor level, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, List<StructureTemplate.StructureEntityInfo> structureEntityInfoList) {
        List<StructureTemplate.StructureEntityInfo> list = Lists.newArrayList();
        for(StructureTemplate.StructureEntityInfo entityInfo : structureEntityInfoList) {
            Vec3 pos = StructureTemplateInjection.transformedVec3d(structurePlaceSettings, entityInfo.pos).add(Vec3.atLowerCornerOf(blockPos));
            BlockPos blockpos = StructureTemplate.calculateRelativePosition(structurePlaceSettings, entityInfo.blockPos).offset(blockPos);
            StructureTemplate.StructureEntityInfo info = new StructureTemplate.StructureEntityInfo(pos, blockpos, entityInfo.nbt);
            for (StructureProcessor proc : structurePlaceSettings.getProcessors()) {
                info = proc.processEntity(level, blockPos, entityInfo, info, structurePlaceSettings, template);
                if (info == null)
                    break;
            }
            if (info != null)
                list.add(info);
        }

        return list;
    }
}
