package xyz.bluspring.kilt.injects.server.network;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;

@Mixin(ConfigurationTask.class)
public interface ConfigurationTaskInject {
    @Mixin(ConfigurationTask.Type.class)
    abstract class TypeInject {
        public TypeInject(String id) {}

        @CreateInitializer
        public TypeInject(Identifier id) {
            this(id.toString());
        }
    }
}
