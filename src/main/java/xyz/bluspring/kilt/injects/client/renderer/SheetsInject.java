// TRACKED HASH: 230230582e2edceefcbd96d5a8ae8fde240fa88d
package xyz.bluspring.kilt.injects.client.renderer;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.renderer.SheetsInjection;
import xyz.bluspring.kilt.mixin.SheetsAccessor;
import xyz.bluspring.kilt.workarounds.ResyncingHashMap;

import java.util.Map;

// Move this to an incredibly high priority, to ensure that our custom resync map gets applied.
@Mixin(value = Sheets.class, priority = 5000)
public abstract class SheetsInject implements SheetsInjection {
    @Shadow @Final @Mutable
    public static Map<ResourceKey<BannerPattern>, Material> BANNER_MATERIALS;

    @Shadow @Final @Mutable
    public static Map<ResourceKey<BannerPattern>, Material> SHIELD_MATERIALS;

    @CreateStatic
    private static void addWoodType(WoodType woodType) {
        SheetsInjection.addWoodType(woodType);
    }

    // Kilt: handled by Fabric API
    /*@Redirect(method = "createSignMaterial", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private static ResourceLocation kilt$useResourceNamespace(String location, @Local(argsOnly = true) WoodType type) {
        var loc = new ResourceLocation(type.name());
        return new ResourceLocation(loc.getNamespace(), "entity/signs/" + loc.getPath());
    }

    @Redirect(method = "createHangingSignMaterial", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private static ResourceLocation kilt$useResourceNamespaceForHanging(String location, @Local(argsOnly = true) WoodType type) {
        var loc = new ResourceLocation(type.name());
        return new ResourceLocation(loc.getNamespace(), "entity/signs/hanging/" + loc.getPath());
    }*/

    // Kilt: otherwise the patterns don't exist if Fabric mods load them first, and Supplementaries panics.
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$wrapMapWithResyncing(CallbackInfo ci) {
        BANNER_MATERIALS = new ResyncingHashMap<>(BANNER_MATERIALS, BuiltInRegistries.BANNER_PATTERN.registryKeySet(), SheetsAccessor::callCreateBannerMaterial);
        SHIELD_MATERIALS = new ResyncingHashMap<>(SHIELD_MATERIALS, BuiltInRegistries.BANNER_PATTERN.registryKeySet(), SheetsAccessor::callCreateShieldMaterial);
    }
}