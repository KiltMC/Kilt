package xyz.bluspring.kilt.injections.world.level.gamerules;

import java.util.function.Supplier;

import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.level.gamerules.GameRuleType;

public interface GameRuleTypeInjection {
    Supplier<ExtensionInfo> EXTENSION_INFO = KiltHelper.extensionInfoGetter(GameRuleType.class);

    static ExtensionInfo getExtensionInfo() {
        return EXTENSION_INFO.get();
    }
}
