package net.minecraftforge.common

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BiomeColors
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.RangedAttribute
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.pathfinder.BlockPathTypes
import net.minecraftforge.client.common.IClientFluidTypeExtensions
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import net.minecraftforge.registries.holderset.AnyHolderSet
import net.minecraftforge.registries.holderset.HolderSetType
import xyz.bluspring.kilt.workarounds.ForgeModWorkaround
import java.util.function.Consumer

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

        private val VANILLA_FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, "minecraft")

        @JvmField
        val EMPTY_TYPE = VANILLA_FLUID_TYPES.register("empty") {
            EmptyFluidType()
        }

        @JvmField
        val WATER_TYPE = VANILLA_FLUID_TYPES.register("water") {
            WaterFluidType()
        }

        class EmptyFluidType : FluidType(
            Properties.create()
                .apply {
                    descriptionId("block.minecraft.air")
                    motionScale(1.0)
                    canPushEntity(false)
                    canSwim(false)
                    canDrown(false)
                    fallDistanceModifier(1F)
                    pathType(null)
                    adjacentPathType(null)
                    density(0)
                    temperature(0)
                    viscosity(0)
                }
        ) {
            override fun setItemMovement(entity: Entity) {
                if (!entity.isNoGravity)
                    entity.deltaMovement = entity.deltaMovement.add(0.0, -.04, 0.0)
            }
        }

        class WaterFluidType : FluidType(
            Properties.create()
                .apply {
                    descriptionId("block.minecraft.water")
                    canExtinguish(true)
                    fallDistanceModifier(0F)
                    canConvertToSource(true)
                    supportsBoating(true)
                    sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                    canHydrate(true)
                }
        ) {
            override fun getBlockPathType(
                state: FluidState,
                level: BlockGetter,
                pos: BlockPos,
                mob: Mob?,
                canFluidLog: Boolean
            ): BlockPathTypes? {
                return if (canFluidLog)
                    super.getBlockPathType(state, level, pos, mob, true)
                else null
            }

            override fun initializeClient(consumer: Consumer<IClientFluidTypeExtensions>) {
                consumer.accept(object : IClientFluidTypeExtensions {
                    val UNDERWATER_LOCATION = ResourceLocation("textures/misc/underwater.png")
                    val WATER_STILL = ResourceLocation("block/water_still")
                    val WATER_FLOW = ResourceLocation("block/water_flow")
                    val WATER_OVERLAY = ResourceLocation("block/water_overlay")

                    override val stillTexture: ResourceLocation
                        get() = WATER_STILL

                    override val flowingTexture: ResourceLocation
                        get() = WATER_FLOW

                    override val overlayTexture: ResourceLocation
                        get() = WATER_OVERLAY

                    override fun getRenderOverlayTexture(mc: Minecraft): ResourceLocation {
                        return UNDERWATER_LOCATION
                    }

                    override val tintColor: Int
                        get() = 0xFF3F76E4.toInt()

                    override fun getTintColor(state: FluidState, getter: BlockAndTintGetter, pos: BlockPos): Int {
                        return BiomeColors.getAverageWaterColor(getter, pos) or 0xFF000000.toInt()
                    }
                })
            }
        }

        private var enableMilkFluid = false

        @JvmStatic
        fun enableMilkFluid() {
            enableMilkFluid = true
        }
    }
}