package xyz.bluspring.kilt.injections.client.renderer;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.WoodType;

public interface SheetsInjection {
    static void addWoodType(WoodType woodType) {
        Sheets.SIGN_MATERIALS.put(woodType, createSignMaterial(woodType));
    }

    static Material createSignMaterial(WoodType woodType) {
        var location = new ResourceLocation(woodType.name());
        return new Material(Sheets.SIGN_SHEET, new ResourceLocation(location.getNamespace(), "entity/signs/" + location.getPath()));
    }
}
