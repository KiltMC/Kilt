package xyz.bluspring.kilt.injects.world.level.gamerules;

import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.NamedEnum;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import net.minecraft.world.level.gamerules.GameRuleType;

@NamedEnum
@Mixin(GameRuleType.class)
public abstract class GameRuleTypeInject {
    @CreateStatic
    private static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(GameRuleType.class);
    }
}
