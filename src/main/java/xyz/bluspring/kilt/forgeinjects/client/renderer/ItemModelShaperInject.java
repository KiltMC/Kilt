package xyz.bluspring.kilt.forgeinjects.client.renderer;

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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemModelShaper.class)
public abstract class ItemModelShaperInject {
    @Unique
    private ForgeItemModelShaper kilt$forgeModelShaper;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initForgeModelShaper(ModelManager modelManager, CallbackInfo ci) {
        if (!((Object) this instanceof ForgeItemModelShaper)) // we're not running recursively
            this.kilt$forgeModelShaper = new ForgeItemModelShaper(modelManager);
    }

    @Inject(method = "getItemModel(Lnet/minecraft/world/item/Item;)Lnet/minecraft/client/resources/model/BakedModel;", at = @At("HEAD"), cancellable = true)
    private void kilt$useForgeItemModel(Item item, CallbackInfoReturnable<BakedModel> cir) {
        // TODO: Run a check that ensures the model namespace is a Forge mod ID?
        cir.setReturnValue(this.kilt$forgeModelShaper.getItemModel(item));
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
