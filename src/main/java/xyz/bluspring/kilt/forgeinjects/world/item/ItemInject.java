// TRACKED HASH: 9eea5d52613b4f5dd7ab28464cb20635fe94ca6d
package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.injections.client.renderer.RenderPropertiesInjection;
import xyz.bluspring.kilt.injections.item.ItemInjection;
import xyz.bluspring.kilt.injections.item.ItemPropertiesInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(Item.class)
public abstract class ItemInject implements IForgeItem, ItemInjection, RenderPropertiesInjection {
    @Shadow @Final @Mutable
    public static Map<Block, Item> BY_BLOCK;
    private boolean canRepair;
    private Object renderProperties;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$setRepairability(Item.Properties properties, CallbackInfo ci) {
        canRepair = ((ItemPropertiesInjection) properties).getCanRepair();
        kilt$initClient();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$useForgeBlockItemMap(CallbackInfo ci) {
        //BY_BLOCK = GameData.getBlockItemMap();
    }

    @WrapOperation(method = "isEnchantable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getMaxStackSize()I"))
    private int kilt$tryUseForgeMaxStackSize(Item instance, Operation<Integer> original, @Local(argsOnly = true) ItemStack stack) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Item.class, "getMaxStackSize", ItemStack.class)) {
            return instance.getMaxStackSize(stack);
        }

        return original.call(instance);
    }

    private void kilt$initClient() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            try {
                this.initializeClient(properties -> {
                    this.renderProperties = properties;
                });
            } catch (Throwable e) {
                Kilt.Companion.getLogger().error("Failed to initialize client properties for item {}! This may result in a game crash.", this.getClass());
                e.printStackTrace();
            }
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