package net.minecraftforge.common.extensions

import io.github.fabricators_of_create.porting_lib.util.ToolAction
import io.github.fabricators_of_create.porting_lib.util.ToolActions
import net.minecraft.client.Camera
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.*
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.pathfinder.BlockPathTypes
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.IPlantable
import java.util.*
import java.util.function.BiConsumer

interface IForgeBlockState {
    private fun self(): BlockState {
        return this as BlockState
    }
    
    private fun block(): IForgeBlock {
        return block() as IForgeBlock
    }
    
    fun getFriction(level: LevelReader, pos: BlockPos, entity: Entity?): Float {
        return block().getFriction(self(), level, pos, entity)
    }

    fun getLightEmission(level: BlockGetter, pos: BlockPos): Int {
        return block().getLightEmission(self(), level, pos)
    }

    fun isLadder(level: LevelReader, pos: BlockPos, entity: LivingEntity): Boolean {
        return block().isLadder(self(), level, pos, entity)
    }

    fun canHarvestBlock(level: BlockGetter, pos: BlockPos, player: Player): Boolean {
        return block().canHarvestBlock(self(), level, pos, player)
    }

    fun onDestroyedByPlayer(
        level: Level,
        pos: BlockPos,
        player: Player,
        willHarvest: Boolean,
        fluid: FluidState
    ): Boolean {
        return block().onDestroyedByPlayer(self(), level, pos, player, willHarvest, fluid)
    }

    fun isBed(level: BlockGetter, pos: BlockPos, sleeper: LivingEntity?): Boolean {
        return block().isBed(self(), level, pos, sleeper)
    }

    fun isValidSpawn(level: LevelReader, pos: BlockPos, type: SpawnPlacements.Type, entityType: EntityType<*>): Boolean {
        return block().isValidSpawn(self(), level, pos, type, entityType)
    }

    fun getRespawnPosition(
        type: EntityType<*>,
        level: LevelReader,
        pos: BlockPos,
        orientation: Float,
        entity: LivingEntity?
    ): Optional<Vec3> {
        return block().getRespawnPosition(self(), type, level, pos, orientation, entity)
    }

    fun setBedOccupied(level: Level, pos: BlockPos, sleeper: LivingEntity, occupied: Boolean) {
        block().setBedOccupied(self(), level, pos, sleeper, occupied)
    }

    fun getBedDirection(level: LevelReader, pos: BlockPos): Direction {
        return block().getBedDirection(self(), level, pos)
    }

    fun getExplosionResistance(level: BlockGetter, pos: BlockPos, explosion: Explosion): Float {
        return block().getExplosionResistance(self(), level, pos, explosion)
    }

    fun getCloneItemStack(target: HitResult, level: BlockGetter, pos: BlockPos, player: Player): ItemStack {
        return block().getCloneItemStack(self(), target, level, pos, player)
    }

    fun addLandingEffects(
        level: ServerLevel,
        pos: BlockPos,
        state2: BlockState,
        entity: LivingEntity,
        numberOfParticles: Int
    ): Boolean {
        return block().addLandingEffects(self(), level, pos, state2, entity, numberOfParticles)
    }

    fun addRunningEffects(level: Level, pos: BlockPos, entity: Entity): Boolean {
        return block().addRunningEffects(self(), level, pos, entity)
    }

    fun canSustainPlant(level: BlockGetter, pos: BlockPos, facing: Direction, plantable: IPlantable): Boolean {
        return block().canSustainPlant(self(), level, pos, facing, plantable)
    }

    fun onTreeGrow(
        level: LevelReader,
        placeFunction: BiConsumer<BlockPos, BlockState>,
        randomSource: RandomSource,
        pos: BlockPos,
        config: TreeConfiguration
    ): Boolean {
        return block().onTreeGrow(self(), level, placeFunction, randomSource, pos, config)
    }

    fun isFertile(level: BlockGetter, pos: BlockPos): Boolean {
        return block().isFertile(self(), level, pos)
    }

    fun isConduitFrame(level: LevelReader, pos: BlockPos, conduit: BlockPos): Boolean {
        return block().isConduitFrame(self(), level, pos, conduit)
    }

    fun isPortalFrame(level: BlockGetter, pos: BlockPos): Boolean {
        return block().isPortalFrame(self(), level, pos)
    }

    fun getExpDrop(
        level: LevelReader,
        randomSource: RandomSource,
        pos: BlockPos,
        fortuneLevel: Int,
        silkTouchLevel: Int
    ): Int {
        return block().getExpDrop(self(), level, randomSource, pos, fortuneLevel, silkTouchLevel)
    }

    fun rotate(level: LevelAccessor, pos: BlockPos, direction: Rotation): BlockState? {
        return block().rotate(self(), level, pos, direction)
    }

    fun getEnchantPowerBonus(level: LevelReader, pos: BlockPos): Float {
        return block().getEnchantPowerBonus(self(), level, pos)
    }

    fun onNeighborChange(level: LevelReader, pos: BlockPos, neighbor: BlockPos) {
        block().onNeighborChange(self(), level, pos, neighbor)
    }

    fun shouldCheckWeakPower(level: LevelReader, pos: BlockPos, side: Direction): Boolean {
        return block().shouldCheckWeakPower(self(), level, pos, side)
    }

    fun getWeakChanges(level: LevelReader, pos: BlockPos): Boolean {
        return block().getWeakChanges(self(), level, pos)
    }

    fun getSoundType(level: LevelReader, pos: BlockPos, entity: Entity): SoundType {
        return block().getSoundType(self(), level, pos, entity)
    }

    fun getBeaconColorMultiplier(level: LevelReader, pos: BlockPos, beacon: BlockPos): FloatArray? {
        return block().getBeaconColorMultiplier(self(), level, pos, beacon)
    }

    fun getStateAtViewpoint(level: BlockGetter, pos: BlockPos, viewpoint: Vec3): BlockState? {
        return self().block.getStateAtViewpoint(self(), level, pos, viewpoint)
    }

    val isSlimeBlock: Boolean
        get() = block().isSlimeBlock(self())

    val isStickyBlock: Boolean
        get() = block().isStickyBlock(self())

    fun canStickTo(other: BlockState): Boolean {
        return block().canStickTo(self(), other)
    }

    fun getFlammability(level: BlockGetter, pos: BlockPos, face: Direction): Int {
        return block().getFlammability(self(), level, pos, face)
    }

    fun isFlammable(level: BlockGetter, pos: BlockPos, face: Direction): Boolean {
        return block().isFlammable(self(), level, pos, face)
    }

    fun onCaughtFire(level: Level, pos: BlockPos, face: Direction?, igniter: LivingEntity?) {
        block().onCaughtFire(self(), level, pos, face, igniter)
    }

    fun getFireSpreadSpeed(level: BlockGetter, pos: BlockPos, face: Direction): Int {
        return block().getFireSpreadSpeed(self(), level, pos, face)
    }

    fun isFireSource(level: LevelReader, pos: BlockPos, side: Direction): Boolean {
        return block().isFireSource(self(), level, pos, side)
    }

    fun canEntityDestroy(level: BlockGetter, pos: BlockPos, entity: Entity): Boolean {
        return block().canEntityDestroy(self(), level, pos, entity)
    }

    fun isBurning(level: BlockGetter, pos: BlockPos): Boolean {
        return block().isBurning(self(), level, pos)
    }

    fun getBlockPathType(level: BlockGetter, pos: BlockPos, mob: Mob?): BlockPathTypes? {
        return block().getBlockPathType(self(), level, pos, mob)
    }

    fun getAdjacentBlockPathType(
        level: BlockGetter,
        pos: BlockPos,
        mob: Mob?,
        originalType: BlockPathTypes
    ): BlockPathTypes? {
        return block().getAdjacentBlockPathType(self(), level, pos, mob, originalType)
    }

    fun canDropFromExplosion(level: BlockGetter, pos: BlockPos, explosion: Explosion): Boolean {
        return block().canDropFromExplosion(self(), level, pos, explosion)
    }

    fun onBlockExploded(level: Level, pos: BlockPos, explosion: Explosion) {
        block().onBlockExploded(self(), level, pos, explosion)
    }

    fun collisionExtendsVertically(level: BlockGetter, pos: BlockPos, collidingEntity: Entity): Boolean {
        return block().collisionExtendsVertically(self(), level, pos, collidingEntity)
    }

    fun shouldDisplayFluidOverlay(level: BlockAndTintGetter, pos: BlockPos, fluidState: FluidState): Boolean {
        return block().shouldDisplayFluidOverlay(self(), level, pos, fluidState)
    }

    fun getToolModifiedState(context: UseOnContext, toolAction: ToolAction, simulate: Boolean): BlockState? {
        val eventState: BlockState = ForgeEventFactory.onToolUse(self(), context, toolAction, simulate)
        return if (eventState !== self()) eventState else block().getToolModifiedState(
            self(),
            context,
            toolAction,
            simulate
        )
    }

    fun isScaffolding(entity: LivingEntity): Boolean {
        return block().isScaffolding(self(), entity.level, entity.blockPosition(), entity)
    }

    fun canRedstoneConnectTo(level: BlockGetter, pos: BlockPos, direction: Direction?): Boolean {
        return block().canConnectRedstone(self(), level, pos, direction)
    }

    fun hidesNeighborFace(level: BlockGetter, pos: BlockPos, neighborState: BlockState, dir: Direction): Boolean {
        return block().hidesNeighborFace(level, pos, self(), neighborState, dir)
    }

    fun supportsExternalFaceHiding(): Boolean {
        return block().supportsExternalFaceHiding(self())
    }

    fun onBlockStateChange(level: LevelReader, pos: BlockPos, oldState: BlockState) {
        block().onBlockStateChange(level, pos, oldState, self())
    }

    fun canBeHydrated(getter: BlockGetter, pos: BlockPos, fluid: FluidState, fluidPos: BlockPos): Boolean {
        return block().canBeHydrated(self(), getter, pos, fluid, fluidPos)
    }

    fun getAppearance(
        level: BlockAndTintGetter,
        pos: BlockPos,
        side: Direction,
        queryState: BlockState,
        queryPos: BlockPos
    ): BlockState {
        return block().getAppearance(self(), level, pos, side, queryState, queryPos)
    }
}
