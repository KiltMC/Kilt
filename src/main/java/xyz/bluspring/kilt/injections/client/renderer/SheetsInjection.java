package xyz.bluspring.kilt.injections.client.renderer;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.properties.WoodType;
import xyz.bluspring.kilt.mixin.SheetsAccessor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface SheetsInjection {
    static void addWoodType(WoodType woodType) {
        Sheets.SIGN_MATERIALS.put(woodType, SheetsAccessor.callCreateSignMaterial(woodType));
        Sheets.HANGING_SIGN_MATERIALS.put(woodType, SheetsAccessor.callCreateHangingSignMaterial(woodType));
    }
}
