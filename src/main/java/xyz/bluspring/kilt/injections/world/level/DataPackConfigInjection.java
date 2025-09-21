package xyz.bluspring.kilt.injections.world.level;

import net.minecraft.world.level.DataPackConfig;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import java.util.List;

@FabricInjectedInterface(DataPackConfig.class)
public interface DataPackConfigInjection {
    default void addModPacks(List<String> modPacks) {
        throw new IllegalStateException();
    }
}
