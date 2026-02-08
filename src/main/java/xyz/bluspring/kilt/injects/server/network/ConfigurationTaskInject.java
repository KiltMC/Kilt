package xyz.bluspring.kilt.injects.server.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@Mixin(ConfigurationTask.class)
public interface ConfigurationTaskInject {
    @Mixin(ConfigurationTask.Type.class)
    abstract class TypeInject {
        public TypeInject(String id) {}

        @CreateInitializer
        public TypeInject(ResourceLocation id) {
            this(id.toString());
        }
    }
}
