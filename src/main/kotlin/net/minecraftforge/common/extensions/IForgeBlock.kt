package net.minecraftforge.common.extensions

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.WitherSkull
import net.minecraft.world.item.*
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.*
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.MaterialColor
import net.minecraft.world.level.pathfinder.BlockPathTypes
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.IPlantable
import net.minecraftforge.common.ToolAction
import net.minecraftforge.common.ToolActions
import java.util.*
import java.util.function.BiConsumer


interface IForgeBlock {
    private fun self(): Block {
        return this as Block
    }

    fun getFriction(state: BlockState, level: LevelReader, pos: BlockPos, entity: Entity?): Float {
        return self().friction
    }

    fun getLightEmission(state: BlockState, level: BlockGetter, pos: BlockPos): Int {
        return state.lightEmission
    }

    fun isLadder(state: BlockState, level: LevelReader, pos: BlockPos, entity: LivingEntity): Boolean {
        return state.`is`(BlockTags.CLIMBABLE)
    }

    fun makesOpenTrapdoorAboveClimbable(state: BlockState, level: LevelReader, pos: BlockPos, trapdoorState: BlockState): Boolean {
        return state.block is LadderBlock && state.getValue(LadderBlock.FACING) == trapdoorState.getValue(TrapDoorBlock.FACING)
    }

    fun isBurning(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return this == Blocks.FIRE || this == Blocks.LAVA
    }

    fun canHarvestBlock(state: BlockState, level: BlockGetter, pos: BlockPos, player: Player): Boolean {
        return ForgeHooks.isCorrectToolForDrops(state, player)
    }

    fun onDestroyedByPlayer(state: BlockState, level: Level, pos: BlockPos, player: Player, willHarvest: Boolean, fluid: FluidState): Boolean {
        self().playerWillDestroy(level, pos, state, player)
        return level.setBlock(pos, fluid.createLegacyBlock(), if (level.isClientSide) 11 else 3)
    }

    fun isBed(state: BlockState, level: BlockGetter, pos: BlockPos, player: Entity?): Boolean {
        return self() is BedBlock
    }

    fun getRespawnPosition(state: BlockState, type: EntityType<*>, levelReader: LevelReader, pos: BlockPos, orientation: Float, entity: LivingEntity?): Optional<Vec3> {
        if (isBed(state, levelReader, pos, entity) && levelReader is Level && BedBlock.canSetSpawn(levelReader))
            return BedBlock.findStandUpPosition(type, levelReader, pos, orientation)

        return Optional.empty()
    }

    fun isValidSpawn(state: BlockState, level: BlockGetter, pos: BlockPos, type: SpawnPlacements.Type, entityType: EntityType<*>): Boolean {
        return state.isValidSpawn(level, pos, entityType)
    }

    fun setBedOccupied(state: BlockState, level: Level, pos: BlockPos, sleeper: LivingEntity, occupied: Boolean) {
        level.setBlock(pos, state.setValue(BedBlock.OCCUPIED, occupied), 3)
    }

    fun getBedDirection(state: BlockState, level: LevelReader, pos: BlockPos): Direction {
        return state.getValue(HorizontalDirectionalBlock.FACING)
    }

    fun getExplosionResistance(state: BlockState, level: BlockGetter, pos: BlockPos, explosion: Explosion): Float {
        return self().explosionResistance
    }

    fun getCloneItemStack(state: BlockState, target: HitResult, level: BlockGetter, pos: BlockPos, player: Player): ItemStack {
        return self().getCloneItemStack(level, pos, state)
    }

    fun addLandingEffects(state1: BlockState, level: ServerLevel, pos: BlockPos, state2: BlockState, entity: LivingEntity, numberOfParticles: Int): Boolean {
        return false
    }

    fun addRunningEffects(state: BlockState, level: Level, pos: BlockPos, entity: Entity): Boolean {
        return false
    }

    fun canSustainPlant(state: BlockState, level: BlockGetter, pos: BlockPos, facing: Direction, plantable: IPlantable): Boolean

    fun onTreeGrow(state: BlockState, level: LevelReader, placeFunction: BiConsumer<BlockPos, BlockState>, randomSource: RandomSource, pos: BlockPos, config: TreeConfiguration): Boolean {
        return false
    }

    fun isFertile(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        if (state.`is`(Blocks.FARMLAND))
            return state.getValue(FarmBlock.MOISTURE) > 0

        return false
    }

    fun isConduitFrame(state: BlockState, level: LevelReader, pos: BlockPos, conduit: BlockPos): Boolean {
        return state.block == Blocks.PRISMARINE || state.block == Blocks.PRISMARINE_BRICKS || state.block == Blocks.SEA_LANTERN || state.block == Blocks.DARK_PRISMARINE
    }

    fun isPortalFrame(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return state.`is`(Blocks.OBSIDIAN)
    }

    fun getExpDrop(state: BlockState, level: LevelReader, randomSource: RandomSource, pos: BlockPos, fortuneLevel: Int, silkTouchLevel: Int): Int {
        return 0
    }

    fun rotate(state: BlockState, level: LevelAccessor, pos: BlockPos, direction: Rotation): BlockState {
        return state.rotate(direction)
    }

    fun getEnchantPowerBonus(state: BlockState, level: LevelReader, pos: BlockPos): Float {
        return if (state.`is`(Blocks.BOOKSHELF)) 1F else 0F
    }

    fun onNeighborChange(state: BlockState, level: LevelReader, pos: BlockPos, neighbor: BlockPos) {}

    fun shouldCheckWeakPower(state: BlockState, level: LevelReader, pos: BlockPos, side: Direction): Boolean {
        return state.isRedstoneConductor(level, pos)
    }

    fun getWeakChanges(state: BlockState, level: LevelReader, pos: BlockPos): Boolean {
        return false
    }

    fun getSoundType(state: BlockState, level: LevelReader, pos: BlockPos, entity: Entity?): SoundType {
        return self().getSoundType(state)
    }

    fun getBeaconColorMultiplier(state: BlockState, level: LevelReader, pos: BlockPos, beaconPos: BlockPos): FloatArray? {
        if (self() is BeaconBeamBlock)
            return (self() as BeaconBeamBlock).color.textureDiffuseColors

        return null
    }

    fun getStateAtViewport(state: BlockState, level: BlockGetter, pos: BlockPos, viewpoint: Vec3): BlockState {
        return state
    }

    fun getBlockPathType(state: BlockState, level: BlockGetter, pos: BlockPos, mob: Mob?): BlockPathTypes? {
        return if (state.block == Blocks.LAVA)
            BlockPathTypes.LAVA
        else if ((state as IForgeBlockState).isBurning(level, pos))
            BlockPathTypes.DAMAGE_FIRE
        else null
    }

    fun getAdjacentBlockPathType(state: BlockState, level: BlockGetter, pos: BlockPos, mob: Mob?, originalType: BlockPathTypes): BlockPathTypes? {
        return if (state.`is`(Blocks.CACTUS))
            BlockPathTypes.DANGER_CACTUS
        else if (state.`is`(Blocks.SWEET_BERRY_BUSH))
            BlockPathTypes.DANGER_OTHER
        else if (WalkNodeEvaluator.isBurningBlock(state))
            BlockPathTypes.DANGER_FIRE
        else null
    }

    fun isSlimeBlock(state: BlockState): Boolean {
        return state.block == Blocks.SLIME_BLOCK
    }

    fun isStickyBlock(state: BlockState): Boolean {
        return state.block == Blocks.SLIME_BLOCK || state.block == Blocks.HONEY_BLOCK
    }

    fun canStickTo(state: BlockState, other: BlockState): Boolean {
        return if (state.block == Blocks.HONEY_BLOCK && other.block == Blocks.SLIME_BLOCK)
            false
        else if (state.block == Blocks.SLIME_BLOCK && other.block == Blocks.HONEY_BLOCK)
            false
        else (state as IForgeBlockState).isStickyBlock || (other as IForgeBlockState).isStickyBlock
    }

    fun getFlammability(state: BlockState, level: BlockGetter, pos: BlockPos, direction: Direction): Int {
        return (Blocks.FIRE as FireBlock).getBurnOdds(state)
    }

    fun isFlammable(state: BlockState, level: BlockGetter, pos: BlockPos, direction: Direction): Boolean {
        return (state as IForgeBlockState).getFlammability(level, pos, direction) > 0
    }

    fun onCaughtFire(state: BlockState, level: Level, pos: BlockPos, direction: Direction?, igniter: LivingEntity?) {}

    fun getFireSpreadSpeed(state: BlockState, level: BlockGetter, pos: BlockPos, direction: Direction): Int {
        return (Blocks.FIRE as FireBlock).getIgniteOdds(state)
    }

    fun isFireSource(state: BlockState, level: LevelReader, pos: BlockPos, direction: Direction): Boolean {
        return state.`is`(level.dimensionType().infiniburn)
    }

    fun canEntityDestroy(state: BlockState, level: BlockGetter, pos: BlockPos, entity: Entity): Boolean {
        return when (entity) {
            is EnderDragon -> !self().defaultBlockState().`is`(BlockTags.DRAGON_IMMUNE)
            is WitherBoss, is WitherSkull -> state.isAir || WitherBoss.canDestroy(state)
            else -> true
        }
    }

    fun canDropFromExplosion(state: BlockState, level: BlockGetter, pos: BlockPos, explosion: Explosion): Boolean {
        return state.block.dropFromExplosion(explosion)
    }

    fun onBlockExploded(state: BlockState, level: Level, pos: BlockPos, explosion: Explosion) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
        self().wasExploded(level, pos, explosion)
    }

    fun collisionExtendsVertically(state: BlockState, level: BlockGetter, pos: BlockPos, collidingEntity: Entity): Boolean {
        return state.`is`(BlockTags.FENCES) || state.`is`(BlockTags.WALLS) || self() is FenceGateBlock
    }

    fun shouldDisplayFluidOverlay(state: BlockState, level: BlockAndTintGetter, pos: BlockPos, fluidState: FluidState): Boolean {
        return state.block is HalfTransparentBlock || state.block is LeavesBlock
    }

    fun getToolModifiedState(
        state: BlockState,
        context: UseOnContext,
        toolAction: ToolAction,
        simulate: Boolean
    ): BlockState? {
        val itemStack = context.itemInHand
        if (!itemStack.canPerformAction(toolAction.fabricToolAction)) return null

        if (ToolActions.AXE_STRIP === toolAction) {
            return AxeItem.getAxeStrippingState(state)
        } else if (ToolActions.AXE_SCRAPE === toolAction) {
            return WeatheringCopper.getPrevious(state).orElse(null)
        } else if (ToolActions.AXE_WAX_OFF === toolAction) {
            return Optional.ofNullable(HoneycombItem.WAX_OFF_BY_BLOCK.get()[state.block]).map { block: Block ->
                block.withPropertiesOf(
                    state
                )
            }.orElse(null)
        } else if (ToolActions.SHOVEL_FLATTEN === toolAction) {
            return ShovelItem.getShovelPathingState(state)
        } else if (ToolActions.HOE_TILL === toolAction) {
            // Logic copied from HoeItem#TILLABLES; needs to be kept in sync during updating
            val block = state.block
            if (block === Blocks.ROOTED_DIRT) {
                if (!simulate && !context.level.isClientSide) {
                    Block.popResourceFromFace(
                        context.level,
                        context.clickedPos,
                        context.clickedFace,
                        ItemStack(Items.HANGING_ROOTS)
                    )
                }
                return Blocks.DIRT.defaultBlockState()
            } else if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT_PATH || block == Blocks.DIRT || block == Blocks.COARSE_DIRT &&
                context.level.getBlockState(context.clickedPos.above()).isAir
            ) {
                return if (block == Blocks.COARSE_DIRT) Blocks.DIRT.defaultBlockState() else Blocks.FARMLAND.defaultBlockState()
            }
        }
        return null
    }

    fun isScaffolding(state: BlockState, level: LevelReader, pos: BlockPos, entity: LivingEntity): Boolean {
        return state.`is`(Blocks.SCAFFOLDING)
    }

    fun canConnectRedstone(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        direction: Direction?
    ): Boolean {
        return if (state.`is`(Blocks.REDSTONE_WIRE)) {
            true
        } else if (state.`is`(Blocks.REPEATER)) {
            val facing = state.getValue(RepeaterBlock.FACING)
            facing == direction || facing.opposite == direction
        } else if (state.`is`(Blocks.OBSERVER)) {
            direction == state.getValue(ObserverBlock.FACING)
        } else {
            state.isSignalSource && direction != null
        }
    }

    fun hidesNeighborFace(
        level: BlockGetter,
        pos: BlockPos,
        state: BlockState,
        neighborState: BlockState,
        dir: Direction
    ): Boolean {
        return false
    }

    fun supportsExternalFaceHiding(state: BlockState?): Boolean {
        return if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            !ForgeHooksClient.isBlockInSolidLayer(state)
        } else true
    }

    fun onBlockStateChange(level: LevelReader, pos: BlockPos, oldState: BlockState, newState: BlockState) {}

    fun canBeHydrated(
        state: BlockState,
        getter: BlockGetter,
        pos: BlockPos,
        fluid: FluidState,
        fluidPos: BlockPos
    ): Boolean {
        return (fluid as IForgeFluidState).canHydrate(getter, fluidPos, state, pos)
    }

    fun getMapColor(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        defaultColor: MaterialColor
    ): MaterialColor {
        return defaultColor
    }

    fun getAppearance(
        state: BlockState,
        level: BlockAndTintGetter,
        pos: BlockPos,
        side: Direction,
        queryState: BlockState?,
        queryPos: BlockPos?
    ): BlockState {
        return state
    }
}