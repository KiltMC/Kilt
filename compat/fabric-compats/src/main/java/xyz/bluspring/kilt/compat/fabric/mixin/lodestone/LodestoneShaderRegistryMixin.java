package xyz.bluspring.kilt.compat.fabric.mixin.lodestone;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import team.lodestar.lodestone.LodestoneLib;
import team.lodestar.lodestone.registry.client.LodestoneShaderRegistry;
import team.lodestar.lodestone.systems.rendering.shader.ShaderHolder;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import java.io.IOException;

@Pseudo
@IfModLoaded("lodestone")
@Mixin(LodestoneShaderRegistry.class)
public class LodestoneShaderRegistryMixin {

    // Copied from https://github.com/LodestarMC/Lodestone/blob/1.20/src/main/java/team/lodestar/lodestone/registry/client/LodestoneShaderRegistry.java#L55-L63
    @CreateStatic
    private static void registerShader(RegisterShadersEvent event, ShaderHolder shaderHolder) {
        try {
            ResourceProvider provider = event.getResourceProvider();
            event.registerShader(shaderHolder.createInstance(provider), shaderHolder::setShaderInstance);
        } catch (IOException e) {
            LodestoneLib.LOGGER.error("Error registering shader", e);
            e.printStackTrace();
        }
    }

}
