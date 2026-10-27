// TRACKED HASH: 47476edb59fcfcf846692ab9ec3986cad3c5d6c4
package xyz.bluspring.kilt.injects.world.item;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.item.SpawnEggItemInjection;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;

@Mixin(SpawnEggItem.class)
public abstract class SpawnEggItemInject extends Item implements SpawnEggItemInjection {
    public SpawnEggItemInject(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable DispenseItemBehavior createDispenseBehavior() {
        return null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void registerDispenseBehavior(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BuiltInRegistries.ITEM.forEach(item -> {
            if (item instanceof SpawnEggItem egg && !DispenserBlock.DISPENSER_REGISTRY.containsKey(egg)) {
                var beh = egg.createDispenseBehavior();
                if (beh != null)
                    DispenserBlock.registerBehavior(egg, beh);
            }
        }));
    }
}
