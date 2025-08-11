// TRACKED HASH: 26c79ae35004b89c78005cfdd41e52154d8a8042
package xyz.bluspring.kilt.injects.client.gui.screens.worldselection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.level.storage.LevelSummaryInjection;
import xyz.bluspring.kilt.mixin.AbstractSelectionListAccessor;

import java.util.List;

@Mixin(WorldSelectionList.class)
public abstract class WorldSelectionListInject {
    private static final ResourceLocation FORGE_EXPERIMENTAL_WARNING_ICON = ResourceLocation.fromNamespaceAndPath("neoforge","textures/gui/experimental_warning.png");

    @Mixin(WorldSelectionList.WorldListEntry.class)
    public abstract static class WorldListEntryInject {
        @Shadow @Final private LevelSummary summary;

        @Shadow @Final private WorldSelectionList field_19135;

        // Kilt: even though this is unused, it's probably used by something
        private void renderExperimentalWarning(GuiGraphics guiGraphics, int mouseX, int mouseY, int top, int left) {
            if (((LevelSummaryInjection) this.summary).isLifecycleExperimental()) {
                int leftStart = left + field_19135.getRowWidth();
                guiGraphics.blit(FORGE_EXPERIMENTAL_WARNING_ICON, leftStart - 36, top, 0.0F, 0.0F, 32, 32, 32, 32);
                if (((AbstractSelectionListAccessor<WorldSelectionList.Entry>) field_19135).callGetEntryAtPosition(mouseX, mouseY) == (Object) this && mouseX > leftStart - 36 && mouseX < leftStart) {
                    var font = Minecraft.getInstance().font;
                    List<FormattedCharSequence> tooltip = font.split(Component.translatable("forge.experimentalsettings.tooltip"), 200);
                    guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
                }
            }
        }
    }
}