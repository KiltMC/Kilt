package xyz.bluspring.kilt.injects.world.level.storage;

import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.level.storage.ServerLevelDataInjection;

@Mixin(ServerLevelData.class)
public interface ServerLevelDataInject extends ServerLevelDataInjection {
}
