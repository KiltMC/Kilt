package xyz.bluspring.kilt.injects.client.renderer;

import java.util.Map;

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

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.state.properties.WoodType;

@Mixin(Sheets.class)
public abstract class SheetsInject implements SheetsInjection {
    @Shadow @Final @Mutable public static Map<ResourceKey<DecoratedPotPattern>, Material> DECORATED_POT_MATERIALS;

    @CreateStatic
    private static void addWoodType(WoodType woodType) {
        SheetsInjection.addWoodType(woodType);
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$handleSheetsLoadedEarly(CallbackInfo ci) {
        // Kilt: Resync all sheet data to ensure things are loaded even if this gets loaded too early.
        DECORATED_POT_MATERIALS = new ResyncingHashMap<>(DECORATED_POT_MATERIALS, BuiltInRegistries.DECORATED_POT_PATTERN.registryKeySet(), key ->
            SheetsAccessor.callCreateDecoratedPotMaterial(BuiltInRegistries.DECORATED_POT_PATTERN.get(key).assetId())
        );
    }
}
