// TRACKED HASH: 5520e72d813534639a4a101cffa56e6e9fe409b0
package xyz.bluspring.kilt.injects.world.level.levelgen.structure.templatesystem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.levelgen.structure.templatesystem.StructureTemplateInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateInject implements StructureTemplateInjection {
    @CreateStatic
    private static Vec3 forge$transformedVec3d(StructurePlaceSettings placementIn, Vec3 pos) {
        return StructureTemplateInjection.transformedVec3d(placementIn, pos);
    }

    @CreateStatic
    private static List<StructureTemplate.StructureEntityInfo> forge$processEntityInfos(@Nullable StructureTemplate template, LevelAccessor level, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, List<StructureTemplate.StructureEntityInfo> structureEntityInfoList) {
        return StructureTemplateInjection.processEntityInfos(template, level, blockPos, structurePlaceSettings, structureEntityInfoList);
    }

    private static final AtomicReference<StructureTemplate> kilt$template = new AtomicReference<>(null);

    @Shadow
    public static Vec3 transform(Vec3 target, Mirror mirror, Rotation rotation, BlockPos centerOffset) {
        return null;
    }

    @CreateStatic
    private static List<StructureTemplate.StructureBlockInfo> processBlockInfos(ServerLevelAccessor level, BlockPos pos, BlockPos pos2, StructurePlaceSettings structurePlaceSettings, List<StructureTemplate.StructureBlockInfo> list, @Nullable StructureTemplate template) {
        kilt$template.set(template);
        return StructureTemplate.processBlockInfos(level, pos, pos2, structurePlaceSettings, list);
    }

    @WrapOperation(method = "processBlockInfos", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureProcessor;processBlock(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;)Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;"))
    private static StructureTemplate.StructureBlockInfo kilt$useForgeProcess(StructureProcessor instance, LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings, Operation<StructureTemplate.StructureBlockInfo> original) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(instance.getClass(), StructureProcessor.class, "process", StructureTemplate.StructureBlockInfo.class, LevelReader.class, BlockPos.class, BlockPos.class, StructureTemplate.StructureBlockInfo.class, StructureTemplate.StructureBlockInfo.class, StructurePlaceSettings.class))
            return instance.process(level, offset, pos, blockInfo, relativeBlockInfo, settings, kilt$template.getAndSet(null));
        else
            return original.call(instance, level, offset, pos, blockInfo, relativeBlockInfo, settings);
    }

    @Mixin(StructureTemplate.Palette.class)
    public abstract static class PaletteInject {
        @Mutable @Shadow @Final private Map<Block, List<StructureTemplate.StructureBlockInfo>> cache;

        @Inject(method = "<init>", at = @At("TAIL"))
        private void kilt$wrapWithConcurrentMap(CallbackInfo ci) {
            // Kilt: We want to make sure we retain any data added in here, just in case any mods added stuff in already.
            this.cache = new ConcurrentHashMap<>(this.cache);
        }
    }
}
