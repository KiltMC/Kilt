package xyz.bluspring.kilt.forgeinjects.client.gui.screens.controls;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.mixin.KeyBindsListAccessor;

@Mixin(KeyBindsList.class)
public class KeyBindsListInject {
    @Mixin(KeyBindsList.KeyEntry.class)
    public static class KeyEntryInject {
        @Shadow @Final private Button changeButton;

        @Shadow @Final private KeyMapping key;

        @Shadow private boolean hasCollision;

        @Shadow @Final private Button resetButton;

        @Shadow @Final KeyBindsList field_2742;

        @Inject(method = "method_19870", at = @At("HEAD"))
        private void kilt$setKeyToDefault(KeyMapping keyMapping, Button button, CallbackInfo ci) {
            ((IForgeKeyMapping) this.key).setToDefault();
        }

        /**
         * @author BluSpring
         * @reason I have no idea how to do this patch otherwise
         */
        // fun fact: mixin was yelling at me saying Overwrite can't reduce the visibility of refreshEntry().
        // refreshEntry is normally protected.
        @Overwrite public void refreshEntry() {
            this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
            this.resetButton.active = !this.key.isDefault();
            this.hasCollision = false;
            MutableComponent mutableComponent = Component.empty();
            if (!this.key.isUnbound()) {
                KeyMapping[] var2 = Minecraft.getInstance().options.keyMappings;

                for (KeyMapping keyMapping : var2) {
                    // this is the patch we're doing.
                    // TODO: can we avoid an overwrite?
                    if ((keyMapping != this.key && this.key.same(keyMapping)) || ((IForgeKeyMapping) keyMapping).hasKeyModifierConflict(this.key)) {
                        if (this.hasCollision) {
                            mutableComponent.append(", ");
                        }

                        this.hasCollision = true;
                        mutableComponent.append(Component.translatable(keyMapping.getName()));
                    }
                }
            }

            if (this.hasCollision) {
                this.changeButton.setMessage(Component.literal("[ ").append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE)).append(" ]").withStyle(ChatFormatting.RED));
                this.changeButton.setTooltip(Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", new Object[]{mutableComponent})));
            } else {
                this.changeButton.setTooltip(null);
            }

            if (((KeyBindsListAccessor) field_2742).getKeyBindsScreen().selectedKey == this.key) {
                this.changeButton.setMessage(Component.literal("> ").append(this.changeButton.getMessage().copy().withStyle(new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.UNDERLINE})).append(" <").withStyle(ChatFormatting.YELLOW));
            }
        }
    }
}
