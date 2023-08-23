package xyz.bluspring.kilt.forgeinjects.client.renderer;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.render.SheetsInjection;

@Mixin(Sheets.class)
public class SheetsInject implements SheetsInjection {
    @CreateStatic
    private static void addWoodType(WoodType woodType) {
        SheetsInjection.addWoodType(woodType);
    }

    @CreateStatic
    private static Material createSignMaterial(WoodType woodType) {
        return SheetsInjection.createSignMaterial(woodType);
    }
}
