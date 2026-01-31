// TRACKED HASH: 9eea5d52613b4f5dd7ab28464cb20635fe94ca6d
package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import net.neoforged.neoforge.internal.RegistrationEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.renderer.RenderPropertiesInjection;
import xyz.bluspring.kilt.injections.world.item.ItemInjection;
import xyz.bluspring.kilt.injections.world.item.ItemPropertiesInjection;
import xyz.bluspring.kilt.mixin.ItemPropertiesAccessor;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(Item.class)
public abstract class ItemInject implements IItemExtension, ItemInjection, RenderPropertiesInjection<IClientItemExtensions> {
    @Shadow @Final @Mutable
    public static Map<Block, Item> BY_BLOCK;
    @Shadow @Final @Mutable private DataComponentMap components;

    private boolean canRepair;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$setRepairability(Item.Properties properties, CallbackInfo ci) {
        canRepair = ((ItemPropertiesInjection) properties).getCanRepair();
    }

    @Override
    public void modifyDefaultComponentsFrom(DataComponentPatch patch) {
        if (!RegistrationEvents.canModifyComponents())
            throw new IllegalStateException("Default components can't be modified now!");

        var builder = DataComponentMap.builder().addAll(this.components);
        patch.entrySet().forEach(entry -> builder.set((DataComponentType) entry.getKey(), entry.getValue().orElse(null)));
        this.components = ItemPropertiesAccessor.getComponentInterner().intern(ItemPropertiesInjection.validateComponents(builder.build()));
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$useForgeBlockItemMap(CallbackInfo ci) {
        //BY_BLOCK = GameData.getBlockItemMap();
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return isDamageable(stack) && canRepair;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    }

    @Mixin(Item.Properties.class)
    public static class PropertiesInject implements ItemPropertiesInjection {
        private boolean canRepair = true;

        @Override
        public Item.Properties setNoRepair() {
            canRepair = false;
            return (Item.Properties) (Object) this;
        }

        @Override
        public boolean getCanRepair() {
            return canRepair;
        }

        @CreateStatic
        private static DataComponentMap validateComponents(DataComponentMap map) {
            return ItemPropertiesInjection.validateComponents(map);
        }
    }

    @Mixin(Item.TooltipContext.class)
    public interface TooltipContextInject extends ItemInjection.TooltipContextInjection {
        @Override
        default Level level() {
            return null;
        }

        @Mixin(targets = "net.minecraft.world.item.Item$TooltipContext$2")
        public abstract static class AnonymousOfLevelInject implements Item.TooltipContext {
            @Override
            public Level level() {
                return null; // Kilt TODO: why is this not auto-targeting.
            }
        }
    }
}