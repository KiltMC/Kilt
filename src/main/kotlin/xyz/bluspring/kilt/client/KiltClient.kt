package xyz.bluspring.kilt.client

import io.github.fabricators_of_create.porting_lib.event.client.ParticleManagerRegistrationCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.Minecraft
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.client.event.RegisterParticleProvidersEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import xyz.bluspring.kilt.Kilt

class KiltClient : ClientModInitializer {
    override fun onInitializeClient() {
        registerFabricEvents()

        hasInitialized = true

        Kilt.loader.mods.forEach { mod ->
            mod.eventBus.post(FMLClientSetupEvent(mod, ModLoadingStage.SIDED_SETUP))
        }
    }

    private fun registerFabricEvents() {
        ParticleManagerRegistrationCallback.EVENT.register {
            // i would call ForgeHooksClient.onRegisterParticleProviders,
            // but that doesn't work. i don't know why. but it just doesn't.
            Kilt.loader.postEvent(RegisterParticleProvidersEvent(Minecraft.getInstance().particleEngine))
        }

        ItemTooltipCallback.EVENT.register { stack, flag, components ->
            MinecraftForge.EVENT_BUS.post(ItemTooltipEvent(stack, null, components, flag))
        }
    }

    companion object {
        var hasInitialized = false
            private set
    }
}