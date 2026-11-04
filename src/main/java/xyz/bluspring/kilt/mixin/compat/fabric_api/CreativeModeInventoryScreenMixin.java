package xyz.bluspring.kilt.mixin.compat.fabric_api;

import com.bawnorton.mixinsquared.TargetHandler;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.gui.screens.inventory.CreativeModeInventoryScreenInjection;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;

@Mixin(value = CreativeModeInventoryScreen.class, priority = 1050)
public abstract class CreativeModeInventoryScreenMixin implements CreativeModeInventoryScreenInjection {

    @SuppressWarnings({"InvalidMemberReference", "MixinAnnotationTarget"})
    @TargetHandler(mixin = "net.fabricmc.fabric.mixin.itemgroup.client.CreativeInventoryScreenMixin", name = "updateSelection")
    @Inject(method = "@MixinSquared:Handler", at = @At("TAIL"))
    private void kilt$fabric_api$updateCurrentPage(CallbackInfo ci) {
        var tabs = ((FabricCreativeInventoryScreen) this).getItemGroupsOnPage(((FabricCreativeInventoryScreen) this).getCurrentPage());
        this.setCurrentPage(new CreativeTabsScreenPage(tabs));
    }

    @SuppressWarnings({"MixinAnnotationTarget", "InvalidMemberReference"})
    @TargetHandler(mixin = "net.fabricmc.fabric.mixin.itemgroup.client.CreativeInventoryScreenMixin", name = "init", prefix = "handler")
    @Inject(method = "@MixinSquared:Handler", at = @At("TAIL"))
    private void kilt$fabric_api$initCurrentPage(CallbackInfo ci) {
        var tabs = ((FabricCreativeInventoryScreen) this).getItemGroupsOnPage(((FabricCreativeInventoryScreen) this).getCurrentPage());
        this.setCurrentPage(new CreativeTabsScreenPage(tabs));
    }
}
