// TRACKED HASH: 9eea5d52613b4f5dd7ab28464cb20635fe94ca6d
package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.RenderPropertiesInjection;
import xyz.bluspring.kilt.injections.world.item.ItemInjection;
import xyz.bluspring.kilt.injections.world.item.ItemPropertiesInjection;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(Item.class)
public abstract class ItemInject implements IItemExtension, ItemInjection, RenderPropertiesInjection<IClientItemExtensions> {
    @Shadow @Final @Mutable
    public static Map<Block, Item> BY_BLOCK;
    private boolean canRepair;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$setRepairability(Item.Properties properties, CallbackInfo ci) {
        canRepair = ((ItemPropertiesInjection) properties).getCanRepair();
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
    }
}