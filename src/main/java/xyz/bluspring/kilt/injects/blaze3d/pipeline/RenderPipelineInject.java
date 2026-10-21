package xyz.bluspring.kilt.injects.blaze3d.pipeline;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.neoforged.neoforge.client.stencil.StencilTest;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.blaze3d.pipeline.RenderPipelineInjection;

import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;

@Mixin(RenderPipeline.class)
public abstract class RenderPipelineInject implements RenderPipelineInjection {
    @Shadow @Final private Identifier location;
    @Shadow @Final private Identifier fragmentShader;
    @Shadow @Final private Identifier vertexShader;
    @Shadow @Final private ShaderDefines shaderDefines;
    @Shadow @Final private List<String> samplers;
    @Shadow @Final private List<RenderPipeline.UniformDescription> uniforms;
    @Shadow @Final private @Nullable DepthStencilState depthStencilState;
    @Shadow @Final private PolygonMode polygonMode;
    @Shadow @Final private boolean cull;
    @Shadow @Final private ColorTargetState colorTargetState;
    @Shadow @Final private VertexFormat vertexFormat;
    @Shadow @Final private VertexFormat.Mode vertexFormatMode;

    @Unique private Optional<StencilTest> stencilTest = Optional.empty();

    protected RenderPipelineInject(final Identifier location, final Identifier vertexShader, final Identifier fragmentShader, final ShaderDefines shaderDefines, final List<String> samplers, final List<RenderPipeline.UniformDescription> uniforms, final ColorTargetState colorTargetState, final @Nullable DepthStencilState depthStencilState, final PolygonMode polygonMode, final boolean cull, final VertexFormat vertexFormat, final VertexFormat.Mode vertexFormatMode, final int sortKey) {
    }

    @CreateInitializer
    protected RenderPipelineInject(final Identifier location, final Identifier vertexShader, final Identifier fragmentShader, final ShaderDefines shaderDefines, final List<String> samplers, final List<RenderPipeline.UniformDescription> uniforms, final ColorTargetState colorTargetState, final @Nullable DepthStencilState depthStencilState, final PolygonMode polygonMode, final boolean cull, final VertexFormat vertexFormat, final VertexFormat.Mode vertexFormatMode, final int sortKey,
                                   Optional<StencilTest> stencilTest) {
        this(location, vertexShader, fragmentShader, shaderDefines, samplers, uniforms, colorTargetState, depthStencilState, polygonMode, cull, vertexFormat, vertexFormatMode, sortKey);
        this.stencilTest = stencilTest;
    }

    @Override
    public Optional<StencilTest> getStencilTest() {
        return this.stencilTest;
    }

    @Override
    public void kilt$setStencilTest(Optional<StencilTest> stencilTest) {
        this.stencilTest = stencilTest;
    }

    @Override
    public RenderPipeline.Builder toBuilder() {
        RenderPipeline.Builder builder = RenderPipeline.builder();
        builder.withLocation(this.location)
            .withFragmentShader(this.fragmentShader)
            .withVertexShader(this.vertexShader);

        if (!this.shaderDefines.isEmpty()) {
            for (Map.Entry<String, String> entry : this.shaderDefines.values().entrySet()) {
                builder.withShaderDefine(entry.getKey(), entry.getValue().contains(".") ? Integer.parseInt(entry.getValue()) : Float.parseFloat(entry.getValue()));
            }

            for (String flag : this.shaderDefines.flags()) {
                builder.withShaderDefine(flag);
            }
        }

        if (!this.samplers.isEmpty()) {
            for (String sampler : this.samplers) {
                builder.withSampler(sampler);
            }
        }

        if (!this.uniforms.isEmpty()) {
            for (RenderPipeline.UniformDescription uniform : this.uniforms) {
                if (uniform.textureFormat() != null)
                    builder.withUniform(uniform.name(), uniform.type(), uniform.textureFormat());
                else
                    builder.withUniform(uniform.name(), uniform.type());
            }
        }

        builder.withDepthStencilState(Optional.ofNullable(this.depthStencilState));
        builder.withPolygonMode(this.polygonMode);
        builder.withCull(this.cull);
        builder.withColorTargetState(this.colorTargetState);
        builder.withVertexFormat(this.vertexFormat, this.vertexFormatMode);

        if (this.stencilTest.isPresent())
            builder.withStencilTest(this.stencilTest.orElseThrow());
        else
            builder.withoutStencilTest();

        return builder;
    }

    @Mixin(RenderPipeline.Builder.class)
    public abstract static class BuilderInject implements BuilderInjection {
        @Unique private Optional<StencilTest> stencilTest = Optional.empty();

        @Override
        public RenderPipeline.Builder withStencilTest(StencilTest stencilTest) {
            this.stencilTest = Optional.of(stencilTest);
            return (RenderPipeline.Builder) (Object) this;
        }

        @Override
        public RenderPipeline.Builder withoutStencilTest() {
            this.stencilTest = Optional.empty();
            return (RenderPipeline.Builder) (Object) this;
        }

        @Inject(method = "withSnippet", at = @At("TAIL"))
        private void kilt$attachStencilTestFromSnippet(RenderPipeline.Snippet snippet, CallbackInfo ci) {
            if (snippet.stencilTest().isPresent()) {
                this.stencilTest = snippet.stencilTest();
            }
        }

        @ModifyReturnValue(method = "buildSnippet", at = @At("RETURN"))
        private RenderPipeline.Snippet kilt$attachStencilTestToSnippet(RenderPipeline.Snippet original) {
            ClientRecordMixinRefMaps.EXTENDED_RENDER_PIPELINE_SNIPPET.put(original, new ClientRecordMixinRefMaps.ExtendedRenderPipelineSnippet(this.stencilTest));
            return original;
        }

        @ModifyReturnValue(method = "build", at = @At("RETURN"))
        private RenderPipeline kilt$attachStencilTestToPipeline(RenderPipeline original) {
            original.kilt$setStencilTest(this.stencilTest);
            return original;
        }
    }

    @Mixin(RenderPipeline.Snippet.class)
    public abstract static class SnippetInject implements SnippetInjection {
        @Override
        public Optional<StencilTest> stencilTest() {
            var extension = ClientRecordMixinRefMaps.EXTENDED_RENDER_PIPELINE_SNIPPET.get(this);
            if (extension != null)
                return extension.stencilTest();

            return Optional.empty();
        }

        public SnippetInject(Optional<Identifier> vertexShader, Optional<Identifier> fragmentShader, Optional<ShaderDefines> shaderDefines, Optional<List<String>> samplers, Optional<List<RenderPipeline.UniformDescription>> uniforms, Optional<ColorTargetState> colorTargetState, Optional<DepthStencilState> depthStencilState, Optional<PolygonMode> polygonMode, Optional<Boolean> cull, Optional<VertexFormat> vertexFormat, Optional<VertexFormat.Mode> vertexFormatMode) {}

        @CreateInitializer
        public SnippetInject(Optional<Identifier> vertexShader, Optional<Identifier> fragmentShader, Optional<ShaderDefines> shaderDefines, Optional<List<String>> samplers, Optional<List<RenderPipeline.UniformDescription>> uniforms, Optional<ColorTargetState> colorTargetState, Optional<DepthStencilState> depthStencilState, Optional<PolygonMode> polygonMode, Optional<Boolean> cull, Optional<VertexFormat> vertexFormat, Optional<VertexFormat.Mode> vertexFormatMode, Optional<StencilTest> stencilTest) {
            this(vertexShader, fragmentShader, shaderDefines, samplers, uniforms, colorTargetState, depthStencilState, polygonMode, cull, vertexFormat, vertexFormatMode);
            ClientRecordMixinRefMaps.EXTENDED_RENDER_PIPELINE_SNIPPET.put((RenderPipeline.Snippet) (Object) this, new ClientRecordMixinRefMaps.ExtendedRenderPipelineSnippet(stencilTest));
        }
    }
}
