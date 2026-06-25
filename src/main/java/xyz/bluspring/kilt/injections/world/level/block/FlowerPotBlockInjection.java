package xyz.bluspring.kilt.injections.world.level.block;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public interface FlowerPotBlockInjection {
    default FlowerPotBlock getEmptyPot() {
        throw new IllegalStateException();
    }

    default Map<Identifier, Supplier<? extends Block>> getFullPotsView() {
        throw new IllegalStateException();
    }

    default void addPlant(Identifier flower, Supplier<? extends Block> fullPot) {
        throw new IllegalStateException();
    }

    static FlowerPotBlock create(@Nullable Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> block, BlockBehaviour.Properties properties) {
        try {
            var initializer = FlowerPotBlock.class.getDeclaredConstructor(Supplier.class, Supplier.class, BlockBehaviour.Properties.class);

            return initializer.newInstance(emptyPot, block, properties);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
