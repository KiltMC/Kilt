package xyz.bluspring.kilt.forgeinjects.client.resources.model;

import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.bluspring.kilt.injections.client.resources.model.ModelBakeryInjection;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryInject implements ModelBakeryInjection {
    @Shadow @Nullable public abstract BakedModel bake(ResourceLocation location, ModelState transform);

    @Shadow @Nullable private AtlasSet atlasSet;
    @Unique
    private final AtomicReference<Function<Material, TextureAtlasSprite>> sprites = new AtomicReference<>();

    @Inject(method = "uploadTextures", at = @At("TAIL"))
    public void kilt$resetGetSprite(TextureManager resourceManager, ProfilerFiller profiler, CallbackInfoReturnable<AtlasSet> cir) {
        this.sprites.set(this.atlasSet::getSprite);
    }

    @Override
    public BakedModel bake(ResourceLocation loc, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
        this.sprites.set(sprites);
        var value = this.bake(loc, state);
        this.sprites.set(this.atlasSet::getSprite);
        return value;
    }

    @Override
    public @Nullable AtlasSet getAtlasSet() {
        return atlasSet;
    }

    @ModifyArgs(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/ItemModelGenerator;generateBlockModel(Ljava/util/function/Function;Lnet/minecraft/client/renderer/block/model/BlockModel;)Lnet/minecraft/client/renderer/block/model/BlockModel;"))
    public void kilt$useForgeSpritesForBlockModel(Args args) {
        args.set(0, this.sprites.get());
    }

    @ModifyArgs(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/UnbakedModel;bake(Lnet/minecraft/client/resources/model/ModelBakery;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/BakedModel;"))
    public void kilt$useForgeSpritesForBake(Args args) {
        args.set(1, this.sprites.get());
    }
}
