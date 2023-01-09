package net.minecraftforge.fluids

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.vehicle.Boat
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Material
import net.minecraft.world.level.pathfinder.BlockPathTypes
import net.minecraft.world.phys.Vec3
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions
import net.minecraftforge.common.ForgeMod
import net.minecraftforge.common.SoundAction
import net.minecraftforge.common.SoundActions
import net.minecraftforge.registries.ForgeRegistries
import java.util.function.Consumer


open class FluidType(private val properties: Properties) {
    val descriptionId: String
        get() {
            if (properties.descriptionId == null)
                properties.descriptionId = Util.makeDescriptionId("fluid_type", ForgeRegistries.FLUID_TYPES.get().getKey(this))

            return properties.descriptionId!!
        }
    val motionScale: Double
        get() {
            return properties.motionScale
        }
    val canPushEntity: Boolean
        get() {
            return properties.canPushEntity
        }
    val canSwim: Boolean
        get() {
            return properties.canSwim
        }
    val canDrown: Boolean
        get() {
            return properties.canDrown
        }
    val fallDistanceModifier: Float
        get() {
            return properties.fallDistanceModifier
        }
    val canExtinguish: Boolean
        get() {
            return properties.canExtinguish
        }
    val canConvertToSource: Boolean
        get() {
            return properties.canConvertToSource
        }
    val supportsBoating: Boolean
        get() {
            return properties.supportsBoating
        }

    val pathType: BlockPathTypes?
        get() {
            return properties.pathType
        }
    val adjacentPathType: BlockPathTypes?
        get() {
            return properties.adjacentPathType
        }
    val canHydrate: Boolean
        get() {
            return properties.canHydrate
        }
    val lightLevel: Int
        get() {
            return properties.lightLevel
        }
    val density: Int
        get() {
            return properties.density
        }
    val temperature: Int
        get() {
            return properties.temperature
        }
    val viscosity: Int
        get() {
            return properties.viscosity
        }
    val rarity: Rarity
        get() {
            return properties.rarity
        }
    val sounds: Map<SoundAction, SoundEvent> = properties.sounds

    init {
        initClient()
    }

    open fun getSound(action: SoundAction): SoundEvent? {
        return this.sounds[action]
    }

    open fun motionScale(entity: Entity): Double {
        return motionScale
    }

    open fun canPushEntity(entity: Entity): Boolean {
        return canPushEntity
    }

    open fun canSwim(entity: Entity): Boolean {
        return canSwim
    }

    open fun getFallDistanceModifier(entity: Entity): Float {
        return fallDistanceModifier
    }

    open fun canExtinguish(entity: Entity): Boolean {
        return canExtinguish
    }

    open fun move(state: FluidState, entity: LivingEntity, movementVector: Vec3, gravity: Double): Boolean {
        return false
    }

    open fun canDrownIn(entity: LivingEntity): Boolean {
        return canDrown
    }

    open fun setItemMovement(entity: Entity) {
        val vec3 = entity.deltaMovement
        entity.setDeltaMovement(vec3.x * .99, vec3.y + (if (vec3.y < .06) 5e-4 else 0.0), vec3.z * .99)
    }

    open fun supportsBoating(boat: Boat): Boolean {
        return supportsBoating
    }

    open fun supportsBoating(state: FluidState, boat: Boat): Boolean {
        return this.supportsBoating(boat)
    }

    open fun canRideVehicleUnder(vehicle: Entity, rider: Entity): Boolean {
        if (this == ForgeMod.WATER_TYPE.get())
            return vehicle.rideableUnderWater()

        return true
    }

    open fun canHydrate(entity: Entity): Boolean {
        return canHydrate
    }

    open fun getSound(entity: Entity, action: SoundAction): SoundEvent? {
        return this.getSound(action)
    }

    open fun canExtinguish(state: FluidState, getter: BlockGetter, pos: BlockPos): Boolean {
        return canExtinguish
    }

    open fun canConvertToSource(state: FluidState, reader: LevelReader, pos: BlockPos): Boolean {
        return canConvertToSource
    }

    open fun getBlockPathType(state: FluidState, level: BlockGetter, pos: BlockPos, mob: Mob?, canFluidLog: Boolean): BlockPathTypes? {
        return pathType
    }

    open fun getAdjacentBlockPathType(state: FluidState, level: BlockGetter, pos: BlockPos, mob: Mob?, originalType: BlockPathTypes): BlockPathTypes? {
        return adjacentPathType
    }

    open fun getSound(player: Player?, getter: BlockGetter, pos: BlockPos, action: SoundAction): SoundEvent? {
        return this.getSound(action)
    }

    // this is the point where i decided to just copy-paste from Forge
    // can you tell?

    open fun canHydrate(
        state: FluidState,
        getter: BlockGetter,
        pos: BlockPos,
        source: BlockState,
        sourcePos: BlockPos
    ): Boolean {
        return canHydrate
    }

    open fun getLightLevel(state: FluidState, getter: BlockAndTintGetter, pos: BlockPos): Int {
        return this.lightLevel
    }

    open fun getDensity(state: FluidState, getter: BlockAndTintGetter, pos: BlockPos): Int {
        return this.density
    }

    open fun getTemperature(state: FluidState, getter: BlockAndTintGetter, pos: BlockPos): Int {
        return this.temperature
    }

    open fun getViscosity(state: FluidState, getter: BlockAndTintGetter, pos: BlockPos): Int {
        return this.viscosity
    }

    open fun canConvertToSource(stack: FluidStack?): Boolean {
        return canConvertToSource
    }

    open fun getSound(stack: FluidStack, action: SoundAction): SoundEvent? {
        return this.getSound(action)
    }

    open fun getDescription(stack: FluidStack): Component? {
        return Component.translatable(getDescriptionId(stack))
    }

    open fun getDescriptionId(stack: FluidStack): String {
        return descriptionId
    }

    open fun canHydrate(stack: FluidStack): Boolean {
        return canHydrate
    }

    open fun getLightLevel(stack: FluidStack): Int {
        return this.lightLevel
    }

    open fun getDensity(stack: FluidStack): Int {
        return this.density
    }

    open fun getTemperature(stack: FluidStack): Int {
        return this.temperature
    }

    open fun getViscosity(stack: FluidStack): Int {
        return this.viscosity
    }

    open fun getRarity(stack: FluidStack): Rarity {
        return rarity
    }

    fun isAir(): Boolean {
        return this == ForgeMod.EMPTY_TYPE.get()
    }

    fun isVanilla(): Boolean {
        return this == ForgeMod.LAVA_TYPE.get() || this == ForgeMod.WATER_TYPE.get()
    }

    open fun getBucket(stack: FluidStack): ItemStack? {
        return ItemStack(stack.fluid.bucket)
    }

    open fun getBlockForFluidState(getter: BlockAndTintGetter?, pos: BlockPos?, state: FluidState): BlockState {
        return state.createLegacyBlock()
    }

    open fun getStateForPlacement(getter: BlockAndTintGetter?, pos: BlockPos?, stack: FluidStack): FluidState {
        return stack.fluid.defaultFluidState()
    }

    open fun canBePlacedInLevel(getter: BlockAndTintGetter?, pos: BlockPos?, state: FluidState): Boolean {
        return !getBlockForFluidState(getter, pos, state).isAir
    }

    open fun canBePlacedInLevel(getter: BlockAndTintGetter?, pos: BlockPos?, stack: FluidStack): Boolean {
        return this.canBePlacedInLevel(getter, pos, getStateForPlacement(getter, pos, stack))
    }

    open fun isLighterThanAir(): Boolean {
        return this.density <= 0
    }

    open fun isVaporizedOnPlacement(level: Level, pos: BlockPos?, stack: FluidStack): Boolean {
        if (level.dimensionType().ultraWarm()) {
            val state = getBlockForFluidState(level, pos, getStateForPlacement(level, pos, stack))
            return state != null && state.material == Material.WATER
        }
        return false
    }

    open fun onVaporize(player: Player?, level: Level, pos: BlockPos, stack: FluidStack?) {
        val sound = this.getSound(player, level, pos, SoundActions.FLUID_VAPORIZE)
        level.playSound(
            player,
            pos,
            sound ?: SoundEvents.FIRE_EXTINGUISH,
            SoundSource.BLOCKS,
            0.5f,
            2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f
        )
        for (l in 0..7) level.addAlwaysVisibleParticle(
            ParticleTypes.LARGE_SMOKE,
            pos.x.toDouble() + Math.random(),
            pos.y.toDouble() + Math.random(),
            pos.z.toDouble() + Math.random(),
            0.0,
            0.0,
            0.0
        )
    }

    override fun toString(): String {
        val name = ForgeRegistries.FLUID_TYPES.get().getKey(this)
        return name?.toString() ?: "Unregistered FluidType"
    }

    // why is this an any type?
    private var renderProperties: Any? = null
    val renderPropertiesInternal: Any?
        get() = renderProperties

    private fun initClient() {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            initializeClient { properties ->
                renderProperties = properties
            }
        }
    }

    open fun initializeClient(consumer: Consumer<IClientFluidTypeExtensions>) {
        // update: this gets used by mods
    }

    companion object {
        @JvmStatic
        val BUCKET_VOLUME = 1000

        val SIZE = net.minecraftforge.common.util.Lazy.of {
            ForgeRegistries.FLUID_TYPES.get().keys.size
        }
    }

    class Properties private constructor() {
        internal var descriptionId: String? = null
        internal var motionScale = 0.014
        internal var canPushEntity = true
        internal var canSwim = true
        internal var canDrown = true
        internal var fallDistanceModifier = .5F
        internal var canExtinguish = false
        internal var canConvertToSource = false
        internal var supportsBoating = false
        internal var pathType: BlockPathTypes? = BlockPathTypes.WATER
        internal var adjacentPathType: BlockPathTypes? = BlockPathTypes.WATER_BORDER
        internal val sounds = mutableMapOf<SoundAction, SoundEvent>()
        internal var canHydrate = false
        internal var lightLevel = 0
        internal var density = 1000
        internal var temperature = 300
        internal var viscosity = 1000
        internal var rarity = Rarity.COMMON

        fun descriptionId(descriptionId: String): Properties {
            this.descriptionId = descriptionId
            return this
        }

        fun motionScale(motionScale: Double): Properties {
            this.motionScale = motionScale
            return this
        }

        fun canPushEntity(canPushEntity: Boolean): Properties {
            this.canPushEntity = canPushEntity
            return this
        }

        fun canSwim(canSwim: Boolean): Properties {
            this.canSwim = canSwim
            return this
        }

        fun canDrown(canDrown: Boolean): Properties {
            this.canDrown = canDrown
            return this
        }

        fun fallDistanceModifier(fallDistanceModifier: Float): Properties {
            this.fallDistanceModifier = fallDistanceModifier
            return this
        }

        fun canExtinguish(canExtinguish: Boolean): Properties {
            this.canExtinguish = canExtinguish
            return this
        }

        fun canConvertToSource(canConvertToSource: Boolean): Properties {
            this.canConvertToSource = canConvertToSource
            return this
        }

        fun supportsBoating(supportsBoating: Boolean): Properties {
            this.supportsBoating = supportsBoating
            return this
        }

        fun pathType(pathType: BlockPathTypes?): Properties {
            this.pathType = pathType
            return this
        }
        fun adjacentPathType(adjacentPathType: BlockPathTypes?): Properties {
            this.adjacentPathType = adjacentPathType
            return this
        }

        fun sound(action: SoundAction, sound: SoundEvent): Properties {
            this.sounds[action] = sound
            return this
        }

        fun canHydrate(canHydrate: Boolean): Properties {
            this.canHydrate = canHydrate
            return this
        }

        fun lightLevel(lightLevel: Int): Properties {
            this.lightLevel = lightLevel
            return this
        }

        fun density(density: Int): Properties {
            this.density = density
            return this
        }

        fun temperature(temperature: Int): Properties {
            this.temperature = temperature
            return this
        }

        fun viscosity(viscosity: Int): Properties {
            this.viscosity = viscosity
            return this
        }

        fun rarity(rarity: Rarity): Properties {
            this.rarity = rarity
            return this
        }

        companion object {
            @JvmStatic
            fun create(): Properties {
                return Properties()
            }
        }
    }
}