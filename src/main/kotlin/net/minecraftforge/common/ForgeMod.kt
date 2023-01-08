package net.minecraftforge.common

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.RangedAttribute
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import net.minecraftforge.registries.holderset.AnyHolderSet
import net.minecraftforge.registries.holderset.HolderSetType
import xyz.bluspring.kilt.workarounds.ForgeModWorkaround

// Apparently Forge scans itself as a mod?
// We can't do that here, so we're gonna have to manually load it in Kilt.
class ForgeMod {
    init {
        INSTANCE = this

        val modEventBus = FMLJavaModLoadingContext.get().getModEventBus()

        
    }

    companion object {
        private lateinit var INSTANCE: ForgeMod
        @JvmStatic
        fun getInstance(): ForgeMod {
            return INSTANCE
        }

        private val ATTRIBUTES = DeferredRegister.create(ForgeRegistries.Keys.ATTRIBUTES, "forge")

        @JvmField
        val MILK_TYPE = RegistryObject.createOptional(ResourceLocation("milk"), ForgeRegistries.Keys.FLUID_TYPES, "minecraft")
        @JvmField
        val MILK = RegistryObject.create(ResourceLocation("milk"), ForgeRegistries.FLUIDS)
        @JvmField
        val FLOWING_MILK = RegistryObject.create(ResourceLocation("flowing_milk"), ForgeRegistries.FLUIDS)

        @JvmField
        val SWIM_SPEED = ATTRIBUTES.register("swim_speed") {
            RangedAttribute("forge.swimSpeed", 1.0, 0.0, 1024.0)
                .setSyncable(true)
        }

        @JvmField
        val ANY_HOLDER_SET: RegistryObject<HolderSetType<*>> = ForgeModWorkaround.ANY_HOLDER_SET
        @JvmField
        val AND_HOLDER_SET: RegistryObject<HolderSetType<*>> = ForgeModWorkaround.AND_HOLDER_SET
        @JvmField
        val OR_HOLDER_SET: RegistryObject<HolderSetType<*>> = ForgeModWorkaround.OR_HOLDER_SET
        @JvmField
        val NOT_HOLDER_SET: RegistryObject<HolderSetType<*>> = ForgeModWorkaround.NOT_HOLDER_SET

        private var enableMilkFluid = false

        @JvmStatic
        fun enableMilkFluid() {
            enableMilkFluid = true
        }
    }
}