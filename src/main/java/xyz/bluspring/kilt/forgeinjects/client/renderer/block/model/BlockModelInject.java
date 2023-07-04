package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(BlockModel.class)
public abstract class BlockModelInject {
    public final BlockGeometryBakingContext customData = new BlockGeometryBakingContext((BlockModel) (Object) this);

    @Shadow public abstract BakedModel bake(ModelBakery modelBakery, BlockModel blockModel, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation, boolean bl);

    /*@ModifyArg(method = "fromStream", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/GsonHelper;fromJson(Lcom/google/gson/Gson;Ljava/io/Reader;Ljava/lang/Class;)Ljava/lang/Object;"))
    private static Gson kilt$useForgeExtendedBlockModelDeserializer(Gson gson) {
        return ExtendedBlockModelDeserializer.INSTANCE;
    }*/

    public BakedModel bakeVanilla(ModelBakery modelBakery, BlockModel blockModel, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation, boolean bl, RenderTypeGroup renderTypes) {
        return UnbakedGeometryHelper.bakeVanilla((BlockModel) (Object) this, modelBakery, blockModel, function, modelState, resourceLocation);
    }
}
