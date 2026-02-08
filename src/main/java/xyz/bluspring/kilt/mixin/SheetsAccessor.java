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
}
