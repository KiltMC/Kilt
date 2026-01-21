package xyz.bluspring.kilt.compat.forgeconfig.mixin;

import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerLifecycleHooks.class)
public interface ServerLifecycleHooksAccessor {
    @Accessor("SERVERCONFIG")
    static LevelResource kilt$getServerConfigLevelResource() {
        throw new IllegalStateException();
    }
}
