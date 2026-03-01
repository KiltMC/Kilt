package xyz.bluspring.kilt.compat.create.registrate

import com.tterrag.registrate.AbstractRegistrate
import com.tterrag.registrate.builders.BuilderCallback
import com.tterrag.registrate.builders.FluidBuilder
import com.tterrag.registrate.util.nullness.NonNullConsumer
import com.tterrag.registrate.util.nullness.NonNullFunction
import com.tterrag.registrate.util.nullness.NonNullSupplier
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.LiquidBlock
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fluids.ForgeFlowingFluid
import net.minecraftforge.registries.ForgeRegistries
import xyz.bluspring.kilt.compat.create.registrate.injects.FluidBuilderInjection

object FluidBuilderHelper {
    private val fluidTypeFactoryClass = Class.forName("com.tterrag.registrate.builders.FluidBuilder\$FluidTypeFactory")

    @JvmStatic
    fun createFluidBuilder(owner: AbstractRegistrate<*>, parent: Any, name: String, callback: BuilderCallback, stillTexture: ResourceLocation, flowingTexture: ResourceLocation): FluidBuilder<*, *> {
        return createFluidBuilder(owner, parent, name, callback, stillTexture, flowingTexture, FluidTypeExtensionHelper::defaultFluidType, ForgeFlowingFluid::Flowing)
    }

    @JvmStatic
    fun createFluidBuilder(owner: AbstractRegistrate<*>, parent: Any, name: String, callback: BuilderCallback, stillTexture: ResourceLocation, flowingTexture: ResourceLocation, typeFactory: FluidTypeFactoryToken): FluidBuilder<*, *> {
        return createFluidBuilder(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, ForgeFlowingFluid::Flowing)
    }

    @JvmStatic
    fun createFluidBuilder(owner: AbstractRegistrate<*>, parent: Any, name: String, callback: BuilderCallback, stillTexture: ResourceLocation, flowingTexture: ResourceLocation, fluidType: NonNullSupplier<FluidType>): FluidBuilder<*, *> {
        return createFluidBuilder(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, ForgeFlowingFluid::Flowing)
    }

    @JvmStatic
    fun createFluidBuilder(owner: AbstractRegistrate<*>, parent: Any, name: String, callback: BuilderCallback, stillTexture: ResourceLocation, flowingTexture: ResourceLocation, fluidFactory: NonNullFunction<ForgeFlowingFluid.Properties, out ForgeFlowingFluid>): FluidBuilder<*, *> {
        return createFluidBuilder(owner, parent, name, callback, stillTexture, flowingTexture, FluidTypeExtensionHelper::defaultFluidType, fluidFactory)
    }

    @JvmStatic
    fun createFluidBuilder(owner: AbstractRegistrate<*>?, parent: Any, name: String?, callback: BuilderCallback?, stillTexture: ResourceLocation?, flowingTexture: ResourceLocation?, typeFactory: FluidTypeFactoryToken?, fluidFactory: NonNullFunction<ForgeFlowingFluid.Properties, out ForgeFlowingFluid>): FluidBuilder<*, *> {
        try {
            val initializer = FluidBuilder::class.java.getDeclaredConstructor(AbstractRegistrate::class.java, Any::class.java, String::class.java, BuilderCallback::class.java, ResourceLocation::class.java, ResourceLocation::class.java, fluidTypeFactoryClass, NonNullFunction::class.java)
            return initializer.newInstance(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, fluidFactory)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun createFluidBuilder(owner: AbstractRegistrate<*>?, parent: Any, name: String?, callback: BuilderCallback?, stillTexture: ResourceLocation?, flowingTexture: ResourceLocation?, fluidType: NonNullSupplier<FluidType>?, fluidFactory: NonNullFunction<ForgeFlowingFluid.Properties, out ForgeFlowingFluid>?): FluidBuilder<*, *> {
        try {
            val initializer = FluidBuilder::class.java.getDeclaredConstructor(AbstractRegistrate::class.java, Any::class.java, String::class.java, BuilderCallback::class.java, ResourceLocation::class.java, ResourceLocation::class.java, fluidTypeFactoryClass, NonNullFunction::class.java)

            return initializer.newInstance(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, fluidFactory)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun createPropertiesConsumer(owner: AbstractRegistrate<*>, name: String, bucketName: String): NonNullConsumer<ForgeFlowingFluid.Properties> {
        return NonNullConsumer<ForgeFlowingFluid.Properties> { properties ->
            properties
                .bucket { owner.get(bucketName, ForgeRegistries.Keys.ITEMS).get() }
                .block { owner.get<Block, LiquidBlock>(name, ForgeRegistries.Keys.BLOCKS).get() }
        }
    }
}
