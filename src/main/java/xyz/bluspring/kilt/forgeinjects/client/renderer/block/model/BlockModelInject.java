package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockModelInjection;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Mixin(BlockModel.class)
public class BlockModelInject implements BlockModelInjection {
    @Shadow @Nullable public ResourceLocation parentLocation;
    @Shadow @Final private List<ItemOverride> overrides;
    @Shadow public String name;
    public final BlockGeometryBakingContext customData = new BlockGeometryBakingContext((BlockModel) (Object) this);

    /*@ModifyArg(method = "fromStream", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/GsonHelper;fromJson(Lcom/google/gson/Gson;Ljava/io/Reader;Ljava/lang/Class;)Ljava/lang/Object;"))
    private static Gson kilt$useForgeExtendedBlockModelDeserializer(Gson gson) {
        return ExtendedBlockModelDeserializer.INSTANCE;
    }*/

    @Inject(method = "getElements", at = @At("HEAD"), cancellable = true)
    private void kilt$cancelIfContainingCustomGeometry(CallbackInfoReturnable<List<BlockElement>> cir) {
        if (this.customData.hasCustomGeometry())
            cir.setReturnValue(Collections.emptyList());
    }

    @Inject(method = "resolveParents", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.BEFORE))
    private void kilt$resolveCustomParents(Function<ResourceLocation, UnbakedModel> resolver, CallbackInfo ci) {
        if (customData.hasCustomGeometry()) {
            customData.getCustomGeometry().resolveParents(resolver, customData);
        }
    }

    /*public BakedModel bakeVanilla(ModelBakery modelBakery, BlockModel blockModel, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation, boolean bl, RenderTypeGroup renderTypes) {
        return UnbakedGeometryHelper.bakeVanilla((BlockModel) (Object) this, modelBakery, blockModel, function, modelState, resourceLocation);
    }*/

    @Override
    public ResourceLocation getParentLocation() {
        return this.parentLocation;
    }

    @Override
    public BlockGeometryBakingContext kilt$getCustomData() {
        return customData;
    }

    @Override
    public ItemOverrides getOverrides(ModelBaker baker, BlockModel blockModel, Function<Material, TextureAtlasSprite> spriteGetter) {
        return this.overrides.isEmpty() ? ItemOverrides.EMPTY : new ItemOverrides(baker, blockModel, this.overrides);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
