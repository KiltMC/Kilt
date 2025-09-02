package xyz.bluspring.kilt.injections.client.renderer;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.properties.WoodType;
import xyz.bluspring.kilt.mixin.SheetsAccessor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface SheetsInjection {
    AtomicBoolean kilt$hasSheetsLoaded = new AtomicBoolean(false);

    static void addWoodType(WoodType woodType) {
        Sheets.SIGN_MATERIALS.put(woodType, SheetsAccessor.callCreateSignMaterial(woodType));
        Sheets.HANGING_SIGN_MATERIALS.put(woodType, SheetsAccessor.callCreateHangingSignMaterial(woodType));
    }

    // otherwise the patterns don't exist if Fabric mods load them first, and Supplementaries panics.
    static void kilt$resyncBannerSheetLayers() {
        if (!kilt$hasSheetsLoaded.get()) // We don't need to resync if it hasn't even loaded yet.
            return;

        SheetsAccessor.setBannerMaterials(BuiltInRegistries.BANNER_PATTERN.registryKeySet().stream().collect(Collectors.toMap(Function.identity(), SheetsAccessor::callCreateBannerMaterial)));
        SheetsAccessor.setShieldMaterials(BuiltInRegistries.BANNER_PATTERN.registryKeySet().stream().collect(Collectors.toMap(Function.identity(), SheetsAccessor::callCreateShieldMaterial)));
    }

    static void kilt$resyncDecoratedPotSheetLayers() {
        if (!kilt$hasSheetsLoaded.get())
            return;

        SheetsAccessor.setDecoratedPotMaterials(BuiltInRegistries.DECORATED_POT_PATTERNS.registryKeySet().stream().collect(Collectors.toMap(Function.identity(), SheetsAccessor::callCreateDecoratedPotMaterial)));
    }
}
