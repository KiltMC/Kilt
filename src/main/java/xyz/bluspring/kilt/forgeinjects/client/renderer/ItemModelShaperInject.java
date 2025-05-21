package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.ForgeItemModelShaper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelShaper.class)
public abstract class ItemModelShaperInject {
    @Unique
    private ForgeItemModelShaper kilt$forgeModelShaper;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initForgeModelShaper(ModelManager modelManager, CallbackInfo ci) {
        if (!((Object) this instanceof ForgeItemModelShaper)) // we're not running recursively
            this.kilt$forgeModelShaper = new ForgeItemModelShaper(modelManager);
    }

    @ModifyReturnValue(method = "getItemModel(Lnet/minecraft/world/item/Item;)Lnet/minecraft/client/resources/model/BakedModel;", at = @At("RETURN"))
    private BakedModel kilt$useForgeItemModel(BakedModel original, @Local(argsOnly = true) Item item) {
        try {
            var model = this.kilt$forgeModelShaper.getItemModel(item);

            if (model == null) {
                return original;
            }

            return model;
        } catch (Exception e) {
            return original;
        }
    }

    @Inject(method = "register", at = @At("TAIL"))
    private void kilt$registerToForgeShaper(Item item, ModelResourceLocation modelLocation, CallbackInfo ci) {
        this.kilt$forgeModelShaper.register(item, modelLocation);
    }

    @Inject(method = "rebuildCache", at = @At("TAIL"))
    private void kilt$rebuildForgeShaperCache(CallbackInfo ci) {
        this.kilt$forgeModelShaper.rebuildCache();
    }
}
