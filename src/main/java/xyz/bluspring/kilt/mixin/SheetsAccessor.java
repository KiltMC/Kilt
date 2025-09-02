package xyz.bluspring.kilt.mixin;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(Sheets.class)
public interface SheetsAccessor {
    @Invoker
    static Material callCreateSignMaterial(WoodType woodType) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static Material callCreateHangingSignMaterial(WoodType woodType) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static Material callCreateBannerMaterial(ResourceKey<BannerPattern> key) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static Material callCreateShieldMaterial(ResourceKey<BannerPattern> key) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static Material callCreateDecoratedPotMaterial(ResourceKey<String> key) {
        throw new UnsupportedOperationException();
    }

    @Accessor("DECORATED_POT_MATERIALS")
    static void setDecoratedPotMaterials(Map<ResourceKey<String>, Material> value) {
        throw new UnsupportedOperationException();
    }

    @Accessor("SHIELD_MATERIALS")
    static void setShieldMaterials(Map<ResourceKey<BannerPattern>, Material> value) {
        throw new UnsupportedOperationException();
    }

    @Accessor("BANNER_MATERIALS")
    static void setBannerMaterials(Map<ResourceKey<BannerPattern>, Material> value) {
        throw new UnsupportedOperationException();
    }
}
