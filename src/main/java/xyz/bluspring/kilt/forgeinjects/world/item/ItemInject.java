// TRACKED HASH: 9eea5d52613b4f5dd7ab28464cb20635fe94ca6d
package xyz.bluspring.kilt.forgeinjects.world.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.RenderPropertiesInjection;
import xyz.bluspring.kilt.injections.item.ItemInjection;
import xyz.bluspring.kilt.injections.item.ItemPropertiesInjection;

import java.util.function.Consumer;

@Mixin(Item.class)
public abstract class ItemInject implements IForgeItem, ItemInjection, RenderPropertiesInjection {
    private boolean canRepair;
    private Object renderProperties;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$setRepairability(Item.Properties properties, CallbackInfo ci) {
        canRepair = ((ItemPropertiesInjection) properties).getCanRepair();
        kilt$initClient();
    }

    private void kilt$initClient() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.initializeClient(properties -> {
                this.renderProperties = properties;
            });
        }
    }

    @Override
    public Object getRenderPropertiesInternal() {
        return this.renderProperties;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return isDamageable(stack) && canRepair;
    }

    @Override
    public void initializeClient(Consumer consumer) {
        ItemInjection.super.initializeClient(consumer);
    }
}