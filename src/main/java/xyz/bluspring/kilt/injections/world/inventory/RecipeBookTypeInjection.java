package xyz.bluspring.kilt.injections.world.inventory;

import java.util.function.Supplier;

import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.inventory.RecipeBookType;

public interface RecipeBookTypeInjection {
    Supplier<ExtensionInfo> EXTENSION_INFO = KiltHelper.extensionInfoGetter(RecipeBookType.class);

    static ExtensionInfo getExtensionInfo() {
        return EXTENSION_INFO.get();
    }
}
