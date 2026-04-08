package xyz.bluspring.kilt.injections.world.level.block.state;

import java.util.function.Supplier;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

public interface BlockBehaviourInjection {
    default boolean isAir(BlockState state) {
        throw KiltHelper.createMixinException(BlockBehaviourInjection.class, "isAir");
    }

    interface PropertiesInjection {
        default Supplier<ResourceKey<LootTable>> getLootTableSupplier() {
            throw new IllegalStateException();
        }
        default BlockBehaviour.Properties lootFrom(Supplier<? extends Block> blockIn) {
            throw new IllegalStateException();
        }
    }

    interface BlockStateBaseInjection {
        default boolean kilt$isAir() {
            throw KiltHelper.createMixinException(BlockStateBaseInjection.class, "kilt$isAir");
        }
    }
}
