package xyz.bluspring.kilt.injections.world.inventory;

import net.minecraft.world.inventory.RecipeBookType;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;

public interface RecipeBookTypeInjection {
    static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(RecipeBookType.class);
    }
}
