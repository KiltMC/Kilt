package xyz.bluspring.kilt.forgeinjects.client.gui.screens.advancements;

import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import java.util.Arrays;

@Mixin(targets = "net/minecraft/client/gui/screens/advancements/AdvancementTabType")
public abstract class AdvancementTabTypeInject {
    @Shadow
    public static AdvancementTabType[] values() {
        throw new IllegalStateException();
    }

    @CreateStatic
    private static final int MAX_TABS = Arrays.stream(values()).mapToInt(AdvancementTabType::getMax).sum();
}
