package xyz.bluspring.kilt.forgeinjects.client.gui.screens.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AdvancementsScreen.class)
public class AdvancementsScreenInject {
    @Shadow @Final private Map<Advancement, AdvancementTab> tabs;
    private static int tabPage, maxPages;

    @Inject(method = "init", at = @At("TAIL"))
    public void kilt$addExtraAdvancementsTabs(CallbackInfo ci) {
        if (this.tabs.size() > AdvancementTabType)
    }
}
