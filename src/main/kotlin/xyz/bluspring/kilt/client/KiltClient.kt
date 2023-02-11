package xyz.bluspring.kilt.client

import io.github.fabricators_of_create.porting_lib.event.client.ParticleManagerRegistrationCallback
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.Minecraft
import net.minecraftforge.client.ForgeHooksClient
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
            ForgeHooksClient.onRegisterParticleProviders(Minecraft.getInstance().particleEngine)
        }
    }

    companion object {
        var hasInitialized = false
            private set
    }
}