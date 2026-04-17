package xyz.bluspring.kilt.injects.client.renderer.chunk;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.chunk.SectionRenderDispatcherInjection;

import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

@Mixin(SectionRenderDispatcher.class)
public abstract class SectionRenderDispatcherInject {
    @Mixin(SectionRenderDispatcher.RenderSection.class)
    public abstract static class RenderSectionInject {
        @Shadow @Final private BlockPos.MutableBlockPos origin;
        @Shadow @Final private SectionRenderDispatcher field_20833;

        @WrapOperation(method = "createCompileTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderRegionCache;createRegion(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/SectionPos;)Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;"))
        private RenderChunkRegion kilt$setRegionEmpty(RenderRegionCache instance, Level level, SectionPos sectionPos, Operation<RenderChunkRegion> original, @Share("additionalRenderers") LocalRef<List<AddSectionGeometryEvent.AdditionalSectionRenderer>> additionalRenderers) {
            try {
                additionalRenderers.set(ClientHooks.gatherAdditionalRenderers(this.origin, field_20833.level));
                instance.kilt$setNullForEmpty(additionalRenderers.get().isEmpty());
                //noinspection MixinExtrasOperationParameters
                return original.call(instance, level, sectionPos);
            } finally {
                instance.kilt$setNullForEmpty(true);
            }
        }

        @ModifyExpressionValue(method = "createCompileTask", at = @At(value = "NEW", target = "(Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;DLnet/minecraft/client/renderer/chunk/RenderChunkRegion;Z)Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection$RebuildTask;"))
        private SectionRenderDispatcher.RenderSection.RebuildTask kilt$attachAdditionalRenderers(SectionRenderDispatcher.RenderSection.RebuildTask original, @Share("additionalRenderers") LocalRef<List<AddSectionGeometryEvent.AdditionalSectionRenderer>> additionalRenderers) {
            ((SectionRenderDispatcherInjection.RenderSectionInjection.RebuildTaskInjection) original).kilt$setAdditionalRenderers(additionalRenderers.get());
            return original;
        }

        @Mixin(SectionRenderDispatcher.RenderSection.RebuildTask.class)
        public abstract static class RebuildTaskInject implements SectionRenderDispatcherInjection.RenderSectionInjection.RebuildTaskInjection {
            @Shadow
            @Final
            private SectionRenderDispatcher.RenderSection field_20839;
            @Unique private List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalRenderers = List.of();

            public RebuildTaskInject(double distAtCreation, @Nullable RenderChunkRegion region, boolean isHighPriority) {
            }

            @CreateInitializer
            public RebuildTaskInject(double distAtCreation, @Nullable RenderChunkRegion region, boolean isHighPriority, List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalRenderers) {
                this(distAtCreation, region, isHighPriority);
                this.additionalRenderers = additionalRenderers;
            }

            @Inject(method = "doTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;setCompiled(Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$CompiledSection;)V"))
            private void kilt$updateGlobalBlockEntities(SectionBufferBuilderPack sectionBufferBuilderPack, CallbackInfoReturnable<CompletableFuture<SectionRenderDispatcher.SectionTaskResult>> cir) {
                field_20839.updateGlobalBlockEntities(Set.of());
            }

            @WrapOperation(method = "doTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/SectionCompiler;compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;"))
            private SectionCompiler.Results kilt$addAdditionalRenderersToCompile(SectionCompiler instance, SectionPos sectionPos, RenderChunkRegion region, VertexSorting vertexSorting, SectionBufferBuilderPack sectionBufferBuilderPack, Operation<SectionCompiler.Results> original) {
                try {
                    instance.kilt$setAdditionalRenderers(this.additionalRenderers);
                    return original.call(instance, sectionPos, region, vertexSorting, sectionBufferBuilderPack);
                } finally {
                    instance.kilt$setAdditionalRenderers(List.of());
                }
            }
        }
    }
}
