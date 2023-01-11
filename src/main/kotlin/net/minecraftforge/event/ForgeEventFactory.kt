package net.minecraftforge.event

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementProgress
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.CommandSelection
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.NonNullList
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.ReloadableServerResources
import net.minecraft.server.level.ChunkHolder
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.players.PlayerList
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.util.random.WeightedRandomList
import net.minecraft.world.Container
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.*
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Player.BedSleepingProblem
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ThrownEnderpearl
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.*
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData
import net.minecraft.world.level.block.LevelEvent
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.portal.PortalShape
import net.minecraft.world.level.storage.PlayerDataStorage
import net.minecraft.world.level.storage.ServerLevelData
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.LootTables
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.ToolAction
import net.minecraftforge.common.util.BlockSnapshot
import net.minecraftforge.common.capabilities.CapabilityDispatcher
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.event.brewing.PlayerBrewedPotionEvent
import net.minecraftforge.event.brewing.PotionBrewEvent
import net.minecraftforge.event.entity.EntityEvent
import net.minecraftforge.event.entity.EntityMobGriefingEvent
import net.minecraftforge.event.entity.EntityMountEvent
import net.minecraftforge.event.entity.EntityStruckByLightningEvent
import net.minecraftforge.event.entity.ProjectileImpactEvent
import net.minecraftforge.event.entity.item.ItemExpireEvent
import net.minecraftforge.event.entity.living.AnimalTameEvent
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.event.entity.living.LivingHealEvent
import net.minecraftforge.event.entity.living.LivingPackSizeEvent
import net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn
import net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent
import net.minecraftforge.event.entity.player.AdvancementEvent.AdvancementEarnEvent
import net.minecraftforge.event.entity.player.AdvancementEvent.AdvancementProgressEvent
import net.minecraftforge.event.entity.player.AdvancementEvent.AdvancementProgressEvent.ProgressType
import net.minecraftforge.event.entity.player.ArrowLooseEvent
import net.minecraftforge.event.entity.player.ArrowNockEvent
import net.minecraftforge.event.entity.player.BonemealEvent
import net.minecraftforge.event.entity.player.EntityItemPickupEvent
import net.minecraftforge.event.entity.player.FillBucketEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.entity.player.PermissionsChangedEvent
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.event.level.BlockEvent.BlockToolModificationEvent
import net.minecraftforge.event.level.BlockEvent.CreateFluidSourceEvent
import net.minecraftforge.event.level.BlockEvent.EntityMultiPlaceEvent
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent
import net.minecraftforge.event.level.BlockEvent.NeighborNotifyEvent
import net.minecraftforge.event.level.ChunkTicketLevelUpdatedEvent
import net.minecraftforge.event.level.PistonEvent
import net.minecraftforge.event.level.SaplingGrowTreeEvent
import net.minecraftforge.event.level.SleepFinishedTimeEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.Event.Result
import net.minecraftforge.fml.LogicalSide
import org.jetbrains.annotations.ApiStatus
import java.io.File
import java.util.*
import java.util.function.BooleanSupplier
import java.util.function.Consumer
import java.util.function.Function


object ForgeEventFactory {
    fun onMultiBlockPlace(entity: Entity?, blockSnapshots: List<BlockSnapshot>, direction: Direction): Boolean {
        val snap = blockSnapshots[0]
        val placedAgainst = snap.level!!.getBlockState(snap.pos.relative(direction.opposite))
        val event = EntityMultiPlaceEvent(blockSnapshots, placedAgainst, entity)
        return MinecraftForge.EVENT_BUS.post(event)
    }

    fun onBlockPlace(entity: Entity?, blockSnapshot: BlockSnapshot, direction: Direction): Boolean {
        val placedAgainst = blockSnapshot.level!!.getBlockState(blockSnapshot.pos.relative(direction.opposite))
        val event = EntityPlaceEvent(blockSnapshot, placedAgainst, entity)
        return MinecraftForge.EVENT_BUS.post(event)
    }

    fun onNeighborNotify(
        level: Level?,
        pos: BlockPos?,
        state: BlockState?,
        notifiedSides: EnumSet<Direction?>?,
        forceRedstoneUpdate: Boolean
    ): NeighborNotifyEvent? {
        val event = NeighborNotifyEvent(level, pos, state, notifiedSides, forceRedstoneUpdate)
        MinecraftForge.EVENT_BUS.post(event)
        return event
    }

    fun doPlayerHarvestCheck(player: Player?, state: BlockState?, success: Boolean): Boolean {
        val event: PlayerEvent.HarvestCheck = HarvestCheck(player, state, success)
        MinecraftForge.EVENT_BUS.post(event)
        return event.canHarvest()
    }

    fun getBreakSpeed(player: Player?, state: BlockState?, original: Float, pos: BlockPos?): Float {
        val event: PlayerEvent.BreakSpeed = BreakSpeed(player, state, original, pos)
        return if (MinecraftForge.EVENT_BUS.post(event)) -1 else event.getNewSpeed()
    }

    fun onPlayerDestroyItem(player: Player?, stack: ItemStack, hand: InteractionHand?) {
        MinecraftForge.EVENT_BUS.post(PlayerDestroyItemEvent(player, stack, hand))
    }

    fun canEntitySpawn(
        entity: Mob?,
        level: LevelAccessor?,
        x: Double,
        y: Double,
        z: Double,
        spawner: BaseSpawner?,
        spawnReason: MobSpawnType?
    ): Result? {
        if (entity == null) return Result.DEFAULT
        val event = CheckSpawn(entity, level, x, y, z, spawner, spawnReason)
        MinecraftForge.EVENT_BUS.post(event)
        return event.getResult()
    }

    fun doSpecialSpawn(
        entity: Mob?,
        level: LevelAccessor?,
        x: Float,
        y: Float,
        z: Float,
        spawner: BaseSpawner?,
        spawnReason: MobSpawnType?
    ): Boolean {
        return MinecraftForge.EVENT_BUS.post(SpecialSpawn(entity, level, x, y, z, spawner, spawnReason))
    }

    fun canEntityDespawn(entity: Mob?): Result? {
        val event = AllowDespawn(entity)
        MinecraftForge.EVENT_BUS.post(event)
        return event.getResult()
    }

    fun getItemBurnTime(itemStack: ItemStack, burnTime: Int, recipeType: RecipeType<*>?): Int {
        val event = FurnaceFuelBurnTimeEvent(itemStack, burnTime, recipeType)
        MinecraftForge.EVENT_BUS.post(event)
        return event.getBurnTime()
    }

    fun getExperienceDrop(entity: LivingEntity?, attackingPlayer: Player?, originalExperience: Int): Int {
        val event = LivingExperienceDropEvent(entity, attackingPlayer, originalExperience)
        return if (MinecraftForge.EVENT_BUS.post(event)) {
            0
        } else event.getDroppedExperience()
    }

    fun getMaxSpawnPackSize(entity: Mob): Int {
        val maxCanSpawnEvent = LivingPackSizeEvent(entity)
        MinecraftForge.EVENT_BUS.post(maxCanSpawnEvent)
        return if (maxCanSpawnEvent.getResult() === Result.ALLOW) maxCanSpawnEvent.getMaxPackSize() else entity.maxSpawnClusterSize
    }

    fun getPlayerDisplayName(player: Player?, username: Component?): Component? {
        val event: PlayerEvent.NameFormat = NameFormat(player, username)
        MinecraftForge.EVENT_BUS.post(event)
        return event.getDisplayname()
    }

    fun getPlayerTabListDisplayName(player: Player?): Component? {
        val event: PlayerEvent.TabListNameFormat = TabListNameFormat(player)
        MinecraftForge.EVENT_BUS.post(event)
        return event.getDisplayName()
    }

    fun fireFluidPlaceBlockEvent(
        level: LevelAccessor?,
        pos: BlockPos?,
        liquidPos: BlockPos?,
        state: BlockState?
    ): BlockState? {
        val event: BlockEvent.FluidPlaceBlockEvent = FluidPlaceBlockEvent(level, pos, liquidPos, state)
        MinecraftForge.EVENT_BUS.post(event)
        return event.getNewState()
    }

    fun onItemTooltip(
        itemStack: ItemStack?,
        entityPlayer: Player?,
        list: List<Component?>?,
        flags: TooltipFlag?
    ): ItemTooltipEvent? {
        val event = ItemTooltipEvent(itemStack, entityPlayer, list, flags)
        MinecraftForge.EVENT_BUS.post(event)
        return event
    }

    fun fireZombieSummonAid(
        zombie: Zombie?,
        level: Level?,
        x: Int,
        y: Int,
        z: Int,
        attacker: LivingEntity?,
        summonChance: Double
    ): SummonAidEvent? {
        val summonEvent = SummonAidEvent(zombie, level, x, y, z, attacker, summonChance)
        MinecraftForge.EVENT_BUS.post(summonEvent)
        return summonEvent
    }

    fun onEntityStruckByLightning(entity: Entity?, bolt: LightningBolt?): Boolean {
        return MinecraftForge.EVENT_BUS.post(EntityStruckByLightningEvent(entity, bolt))
    }

    fun onItemUseStart(entity: LivingEntity?, item: ItemStack?, duration: Int): Int {
        val event: LivingEntityUseItemEvent = Start(entity, item, duration)
        return if (MinecraftForge.EVENT_BUS.post(event)) -1 else event.getDuration()
    }

    fun onItemUseTick(entity: LivingEntity?, item: ItemStack?, duration: Int): Int {
        val event: LivingEntityUseItemEvent = Tick(entity, item, duration)
        return if (MinecraftForge.EVENT_BUS.post(event)) -1 else event.getDuration()
    }

    fun onUseItemStop(entity: LivingEntity?, item: ItemStack?, duration: Int): Boolean {
        return MinecraftForge.EVENT_BUS.post(Stop(entity, item, duration))
    }

    fun onItemUseFinish(entity: LivingEntity?, item: ItemStack?, duration: Int, result: ItemStack?): ItemStack? {
        val event: LivingEntityUseItemEvent.Finish = Finish(entity, item, duration, result)
        MinecraftForge.EVENT_BUS.post(event)
        return event.getResultStack()
    }

    fun onStartEntityTracking(entity: Entity?, player: Player?) {
        MinecraftForge.EVENT_BUS.post(StartTracking(player, entity))
    }

    fun onStopEntityTracking(entity: Entity?, player: Player?) {
        MinecraftForge.EVENT_BUS.post(StopTracking(player, entity))
    }

    fun firePlayerLoadingEvent(player: Player?, playerDirectory: File?, uuidString: String?) {
        MinecraftForge.EVENT_BUS.post(LoadFromFile(player, playerDirectory, uuidString))
    }

    fun firePlayerSavingEvent(player: Player?, playerDirectory: File?, uuidString: String?) {
        MinecraftForge.EVENT_BUS.post(SaveToFile(player, playerDirectory, uuidString))
    }

    fun firePlayerLoadingEvent(player: Player?, playerFileData: PlayerDataStorage, uuidString: String?) {
        MinecraftForge.EVENT_BUS.post(LoadFromFile(player, playerFileData.getPlayerDataFolder(), uuidString))
    }

    fun onToolUse(
        originalState: BlockState?,
        context: UseOnContext?,
        toolAction: ToolAction?,
        simulate: Boolean
    ): BlockState? {
        val event = BlockToolModificationEvent(originalState, context, toolAction, simulate)
        return if (MinecraftForge.EVENT_BUS.post(event)) null else event.getFinalState()
    }

    fun onApplyBonemeal(player: Player, level: Level, pos: BlockPos, state: BlockState, stack: ItemStack): Int {
        val event = BonemealEvent(player, level, pos, state, stack)
        if (MinecraftForge.EVENT_BUS.post(event)) return -1
        if (event.getResult() === Result.ALLOW) {
            if (!level.isClientSide) stack.shrink(1)
            return 1
        }
        return 0
    }

    fun onBucketUse(
        player: Player,
        level: Level,
        stack: ItemStack,
        target: HitResult?
    ): InteractionResultHolder<ItemStack>? {
        val event = FillBucketEvent(player, stack, level, target)
        if (MinecraftForge.EVENT_BUS.post(event)) return InteractionResultHolder(InteractionResult.FAIL, stack)
        if (event.getResult() === Result.ALLOW) {
            if (player.abilities.instabuild) return InteractionResultHolder(InteractionResult.SUCCESS, stack)
            stack.shrink(1)
            if (stack.isEmpty) return InteractionResultHolder(InteractionResult.SUCCESS, event.getFilledBucket())
            if (!player.inventory.add(event.getFilledBucket())) player.drop(event.getFilledBucket(), false)
            return InteractionResultHolder(InteractionResult.SUCCESS, stack)
        }
        return null
    }

    fun onPlaySoundAtEntity(
        entity: Entity?,
        name: SoundEvent?,
        category: SoundSource?,
        volume: Float,
        pitch: Float
    ): PlayLevelSoundEvent.AtEntity? {
        val event: PlayLevelSoundEvent.AtEntity = AtEntity(entity, name, category, volume, pitch)
        MinecraftForge.EVENT_BUS.post(event)
        return event
    }


    fun onPlaySoundAtPosition(
        level: Level?,
        x: Double,
        y: Double,
        z: Double,
        name: SoundEvent?,
        category: SoundSource?,
        volume: Float,
        pitch: Float
    ): PlayLevelSoundEvent.AtPosition? {
        val event: PlayLevelSoundEvent.AtPosition = AtPosition(level, Vec3(x, y, z), name, category, volume, pitch)
        MinecraftForge.EVENT_BUS.post(event)
        return event
    }

    fun onItemExpire(entity: ItemEntity, item: ItemStack): Int {
        if (item.isEmpty) return -1
        val event = ItemExpireEvent(entity, if (item.isEmpty) 6000 else item.item.getEntityLifespan(item, entity.level))
        return if (!MinecraftForge.EVENT_BUS.post(event)) -1 else event.getExtraLife()
    }

    fun onItemPickup(entityItem: ItemEntity?, player: Player?): Int {
        val event: Event = EntityItemPickupEvent(player, entityItem)
        if (MinecraftForge.EVENT_BUS.post(event)) return -1
        return if (event.result == Result.ALLOW) 1 else 0
    }

    fun canMountEntity(entityMounting: Entity, entityBeingMounted: Entity?, isMounting: Boolean): Boolean {
        val isCanceled = MinecraftForge.EVENT_BUS.post(
            EntityMountEvent(
                entityMounting,
                entityBeingMounted,
                entityMounting.level,
                isMounting
            )
        )
        return if (isCanceled) {
            entityMounting.absMoveTo(
                entityMounting.x,
                entityMounting.y,
                entityMounting.z,
                entityMounting.yRotO,
                entityMounting.xRotO
            )
            false
        } else true
    }

    fun onAnimalTame(animal: Animal?, tamer: Player?): Boolean {
        return MinecraftForge.EVENT_BUS.post(AnimalTameEvent(animal, tamer))
    }

    fun onPlayerSleepInBed(player: Player?, pos: Optional<BlockPos?>?): BedSleepingProblem? {
        val event = PlayerSleepInBedEvent(player, pos)
        MinecraftForge.EVENT_BUS.post(event)
        return event.getResultStatus()
    }

    fun onPlayerWakeup(player: Player?, wakeImmediately: Boolean, updateLevel: Boolean) {
        MinecraftForge.EVENT_BUS.post(PlayerWakeUpEvent(player, wakeImmediately, updateLevel))
    }

    fun onPlayerFall(player: Player?, distance: Float, multiplier: Float) {
        MinecraftForge.EVENT_BUS.post(PlayerFlyableFallEvent(player, distance, multiplier))
    }

    fun onPlayerSpawnSet(player: Player?, levelKey: ResourceKey<Level?>?, pos: BlockPos?, forced: Boolean): Boolean {
        return MinecraftForge.EVENT_BUS.post(PlayerSetSpawnEvent(player, levelKey, pos, forced))
    }

    fun onPlayerClone(player: Player?, oldPlayer: Player?, wasDeath: Boolean) {
        MinecraftForge.EVENT_BUS.post(Clone(player, oldPlayer, wasDeath))
    }

    fun onExplosionStart(level: Level?, explosion: Explosion?): Boolean {
        return MinecraftForge.EVENT_BUS.post(Start(level, explosion))
    }

    fun onExplosionDetonate(level: Level?, explosion: Explosion?, list: List<Entity?>?, diameter: Double) {
        //Filter entities to only those who are effected, to prevent modders from seeing more then will be hurt.
        /* Enable this if we get issues with modders looping to much.
        Iterator<Entity> itr = list.iterator();
        Vec3 p = explosion.getPosition();
        while (itr.hasNext())
        {
            Entity e = itr.next();
            double dist = e.getDistance(p.xCoord, p.yCoord, p.zCoord) / diameter;
            if (e.isImmuneToExplosions() || dist > 1.0F) itr.remove();
        }
        */
        MinecraftForge.EVENT_BUS.post(Detonate(level, explosion, list))
    }

    fun onCreateWorldSpawn(level: Level?, settings: ServerLevelData?): Boolean {
        return MinecraftForge.EVENT_BUS.post(CreateSpawnPosition(level, settings))
    }

    fun onLivingHeal(entity: LivingEntity?, amount: Float): Float {
        val event = LivingHealEvent(entity, amount)
        return if (MinecraftForge.EVENT_BUS.post(event)) 0 else event.getAmount()
    }

    fun onPotionAttemptBrew(stacks: NonNullList<ItemStack>): Boolean {
        val tmp = NonNullList.withSize(stacks.size, ItemStack.EMPTY)
        for (x in tmp.indices) tmp[x] = stacks[x].copy()
        val event: PotionBrewEvent.Pre = Pre(tmp)
        if (MinecraftForge.EVENT_BUS.post(event)) {
            var changed = false
            for (x in stacks.indices) {
                changed = changed or ItemStack.matches(tmp[x], stacks[x])
                stacks[x] = event.getItem(x)
            }
            if (changed) onPotionBrewed(stacks)
            return true
        }
        return false
    }

    fun onPotionBrewed(brewingItemStacks: NonNullList<ItemStack>?) {
        MinecraftForge.EVENT_BUS.post(Post(brewingItemStacks))
    }

    fun onPlayerBrewedPotion(player: Player?, stack: ItemStack?) {
        MinecraftForge.EVENT_BUS.post(PlayerBrewedPotionEvent(player, stack))
    }

    fun <T : ICapabilityProvider?> gatherCapabilities(type: Class<out T>?, provider: T): CapabilityDispatcher? {
        return gatherCapabilities(type, provider, null)
    }

    fun <T : ICapabilityProvider?> gatherCapabilities(
        type: Class<out T>?,
        provider: T,
        parent: ICapabilityProvider?
    ): CapabilityDispatcher? {
        return gatherCapabilities(AttachCapabilitiesEvent<T>(type as Class<T>?, provider), parent)
    }

    private fun gatherCapabilities(
        event: AttachCapabilitiesEvent<*>,
        parent: ICapabilityProvider?
    ): CapabilityDispatcher? {
        MinecraftForge.EVENT_BUS.post(event)
        return if (event.getCapabilities().size() > 0 || parent != null) CapabilityDispatcher(
            event.getCapabilities(),
            event.getListeners(),
            parent
        ) else null
    }

    fun fireSleepingLocationCheck(player: LivingEntity, sleepingLocation: BlockPos?): Boolean {
        val evt = SleepingLocationCheckEvent(player, sleepingLocation)
        MinecraftForge.EVENT_BUS.post(evt)
        val canContinueSleep: Result = evt.getResult()
        return if (canContinueSleep == Result.DEFAULT) {
            player.sleepingPos.map(Function<BlockPos, Boolean> { pos: BlockPos? ->
                val state = player.level.getBlockState(pos)
                state.block.isBed(state, player.level, pos, player)
            }).orElse(false)
        } else canContinueSleep == Result.ALLOW
    }

    fun fireSleepingTimeCheck(player: Player, sleepingLocation: Optional<BlockPos?>?): Boolean {
        val evt = SleepingTimeCheckEvent(player, sleepingLocation)
        MinecraftForge.EVENT_BUS.post(evt)
        val canContinueSleep: Result = evt.getResult()
        return if (canContinueSleep == Result.DEFAULT) !player.level.isDay else canContinueSleep == Result.ALLOW
    }

    fun onArrowNock(
        item: ItemStack,
        level: Level?,
        player: Player?,
        hand: InteractionHand?,
        hasAmmo: Boolean
    ): InteractionResultHolder<ItemStack>? {
        val event = ArrowNockEvent(player, item, hand, level, hasAmmo)
        return if (MinecraftForge.EVENT_BUS.post(event)) InteractionResultHolder(
            InteractionResult.FAIL,
            item
        ) else event.getAction()
    }

    fun onArrowLoose(stack: ItemStack?, level: Level?, player: Player?, charge: Int, hasAmmo: Boolean): Int {
        val event = ArrowLooseEvent(player, stack, level, charge, hasAmmo)
        return if (MinecraftForge.EVENT_BUS.post(event)) -1 else event.getCharge()
    }

    fun onProjectileImpact(projectile: Projectile?, ray: HitResult?): Boolean {
        return MinecraftForge.EVENT_BUS.post(ProjectileImpactEvent(projectile, ray))
    }

    fun loadLootTable(name: ResourceLocation?, table: LootTable?, lootTableManager: LootTables?): LootTable? {
        val event = LootTableLoadEvent(name, table, lootTableManager)
        return if (MinecraftForge.EVENT_BUS.post(event)) LootTable.EMPTY else event.getTable()
    }

    fun canCreateFluidSource(level: LevelReader?, pos: BlockPos?, state: BlockState?, def: Boolean): Boolean {
        val evt = CreateFluidSourceEvent(level, pos, state)
        MinecraftForge.EVENT_BUS.post(evt)
        val result: Result = evt.getResult()
        return if (result == Result.DEFAULT) def else result == Result.ALLOW
    }

    fun onTrySpawnPortal(level: LevelAccessor, pos: BlockPos?, size: Optional<PortalShape?>): Optional<PortalShape?>? {
        if (!size.isPresent) return size
        return if (!MinecraftForge.EVENT_BUS.post(
                PortalSpawnEvent(
                    level,
                    pos,
                    level.getBlockState(pos),
                    size.get()
                )
            )
        ) size else Optional.empty()
    }

    fun onEnchantmentLevelSet(
        level: Level?,
        pos: BlockPos?,
        enchantRow: Int,
        power: Int,
        itemStack: ItemStack?,
        enchantmentLevel: Int
    ): Int {
        val e: net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent =
            EnchantmentLevelSetEvent(level, pos, enchantRow, power, itemStack, enchantmentLevel)
        MinecraftForge.EVENT_BUS.post(e)
        return e.getEnchantLevel()
    }

    fun onEntityDestroyBlock(entity: LivingEntity?, pos: BlockPos?, state: BlockState?): Boolean {
        return !MinecraftForge.EVENT_BUS.post(LivingDestroyBlockEvent(entity, pos, state))
    }

    fun getMobGriefingEvent(level: Level, entity: Entity?): Boolean {
        val event = EntityMobGriefingEvent(entity)
        MinecraftForge.EVENT_BUS.post(event)
        val result: Result = event.getResult()
        return if (result == Result.DEFAULT) level.gameRules.getBoolean(GameRules.RULE_MOBGRIEFING) else result == Result.ALLOW
    }

    @Deprecated("")
    fun saplingGrowTree(level: LevelAccessor?, randomSource: RandomSource?, pos: BlockPos?): Boolean {
        return !blockGrowFeature(level, randomSource, pos, null).getResult().equals(Result.DENY)
    }

    fun blockGrowFeature(
        level: LevelAccessor?,
        randomSource: RandomSource?,
        pos: BlockPos?,
        holder: Holder<out ConfiguredFeature<*, *>?>?
    ): SaplingGrowTreeEvent {
        val event = SaplingGrowTreeEvent(level, randomSource, pos, holder)
        MinecraftForge.EVENT_BUS.post(event)
        return event
    }

    fun fireChunkTicketLevelUpdated(
        level: ServerLevel?,
        chunkPos: Long,
        oldTicketLevel: Int,
        newTicketLevel: Int,
        chunkHolder: ChunkHolder?
    ) {
        if (oldTicketLevel != newTicketLevel) MinecraftForge.EVENT_BUS.post(
            ChunkTicketLevelUpdatedEvent(
                level,
                chunkPos,
                oldTicketLevel,
                newTicketLevel,
                chunkHolder
            )
        )
    }

    fun fireChunkWatch(entity: ServerPlayer?, chunk: LevelChunk?, level: ServerLevel?) {
        MinecraftForge.EVENT_BUS.post(Watch(entity, chunk, level))
    }

    fun fireChunkUnWatch(entity: ServerPlayer?, chunkpos: ChunkPos?, level: ServerLevel?) {
        MinecraftForge.EVENT_BUS.post(UnWatch(entity, chunkpos, level))
    }

    fun onPistonMovePre(level: Level?, pos: BlockPos?, direction: Direction?, extending: Boolean): Boolean {
        return MinecraftForge.EVENT_BUS.post(
            Pre(
                level,
                pos,
                direction,
                if (extending) PistonEvent.PistonMoveType.EXTEND else PistonEvent.PistonMoveType.RETRACT
            )
        )
    }

    fun onPistonMovePost(level: Level?, pos: BlockPos?, direction: Direction?, extending: Boolean): Boolean {
        return MinecraftForge.EVENT_BUS.post(
            Post(
                level,
                pos,
                direction,
                if (extending) PistonEvent.PistonMoveType.EXTEND else PistonEvent.PistonMoveType.RETRACT
            )
        )
    }

    fun onSleepFinished(level: ServerLevel?, newTime: Long, minTime: Long): Long {
        val event = SleepFinishedTimeEvent(level, newTime, minTime)
        MinecraftForge.EVENT_BUS.post(event)
        return event.getNewTime()
    }

    fun onResourceReload(serverResources: ReloadableServerResources?): List<PreparableReloadListener?>? {
        val event = AddReloadListenerEvent(serverResources)
        MinecraftForge.EVENT_BUS.post(event)
        return event.getListeners()
    }

    fun onCommandRegister(
        dispatcher: CommandDispatcher<CommandSourceStack?>?,
        environment: CommandSelection?,
        context: CommandBuildContext?
    ) {
        val event = RegisterCommandsEvent(dispatcher, environment, context)
        MinecraftForge.EVENT_BUS.post(event)
    }

    fun getEntitySizeForge(
        entity: Entity?,
        pose: Pose?,
        size: EntityDimensions?,
        eyeHeight: Float
    ): EntityEvent.Size? {
        val evt: EntityEvent.Size = Size(entity, pose, size, eyeHeight)
        MinecraftForge.EVENT_BUS.post(evt)
        return evt
    }

    fun getEntitySizeForge(
        entity: Entity,
        pose: Pose?,
        oldSize: EntityDimensions?,
        newSize: EntityDimensions?,
        newEyeHeight: Float
    ): net.minecraftforge.event.entity.EntityEvent.Size? {
        val evt: EntityEvent.Size = Size(entity, pose, oldSize, newSize, entity.eyeHeight, newEyeHeight)
        MinecraftForge.EVENT_BUS.post(evt)
        return evt
    }

    fun canLivingConvert(
        entity: LivingEntity?,
        outcome: EntityType<out LivingEntity?>?,
        timer: Consumer<Int?>?
    ): Boolean {
        return !MinecraftForge.EVENT_BUS.post(Pre(entity, outcome, timer))
    }

    fun onLivingConvert(entity: LivingEntity?, outcome: LivingEntity?) {
        MinecraftForge.EVENT_BUS.post(Post(entity, outcome))
    }

    fun onEntityTeleportCommand(entity: Entity?, targetX: Double, targetY: Double, targetZ: Double): TeleportCommand? {
        val event = TeleportCommand(entity, targetX, targetY, targetZ)
        MinecraftForge.EVENT_BUS.post(event)
        return event
    }

    fun onEntityTeleportSpreadPlayersCommand(
        entity: Entity?,
        targetX: Double,
        targetY: Double,
        targetZ: Double
    ): SpreadPlayersCommand? {
        val event = SpreadPlayersCommand(entity, targetX, targetY, targetZ)
        MinecraftForge.EVENT_BUS.post(event)
        return event
    }

    fun onEnderTeleport(
        entity: LivingEntity?,
        targetX: Double,
        targetY: Double,
        targetZ: Double
    ): EntityTeleportEvent.EnderEntity? {
        val event: EntityTeleportEvent.EnderEntity = EnderEntity(entity, targetX, targetY, targetZ)
        MinecraftForge.EVENT_BUS.post(event)
        return event
    }


    @ApiStatus.Internal
    @Deprecated("Use {@linkplain #onEnderPearlLand(ServerPlayer, double, double, double, ThrownEnderpearl, float, HitResult) the hit result-sensitive version}.")
    fun onEnderPearlLand(
        entity: ServerPlayer?,
        targetX: Double,
        targetY: Double,
        targetZ: Double,
        pearlEntity: ThrownEnderpearl?,
        attackDamage: Float
    ): EntityTeleportEvent.EnderPearl? {
        return onEnderPearlLand(entity, targetX, targetY, targetZ, pearlEntity, attackDamage, null)
    }

    @ApiStatus.Internal // TODO - 1.20: remove the nullable
    fun onEnderPearlLand(
        entity: ServerPlayer?,
        targetX: Double,
        targetY: Double,
        targetZ: Double,
        pearlEntity: ThrownEnderpearl?,
        attackDamage: Float,
        hitResult: HitResult?
    ): EntityTeleportEvent.EnderPearl? {
        val event: EntityTeleportEvent.EnderPearl =
            EnderPearl(entity, targetX, targetY, targetZ, pearlEntity, attackDamage, hitResult)
        MinecraftForge.EVENT_BUS.post(event)
        return event
    }

    fun onChorusFruitTeleport(
        entity: LivingEntity?,
        targetX: Double,
        targetY: Double,
        targetZ: Double
    ): EntityTeleportEvent.ChorusFruit? {
        val event: EntityTeleportEvent.ChorusFruit = ChorusFruit(entity, targetX, targetY, targetZ)
        MinecraftForge.EVENT_BUS.post(event)
        return event
    }

    fun onPermissionChanged(gameProfile: GameProfile, newLevel: Int, playerList: PlayerList): Boolean {
        val oldLevel = playerList.server.getProfilePermissions(gameProfile)
        val player = playerList.getPlayer(gameProfile.id)
        return if (newLevel != oldLevel && player != null) {
            MinecraftForge.EVENT_BUS.post(PermissionsChangedEvent(player, newLevel, oldLevel))
        } else false
    }

    fun firePlayerChangedDimensionEvent(player: Player?, fromDim: ResourceKey<Level?>?, toDim: ResourceKey<Level?>?) {
        MinecraftForge.EVENT_BUS.post(PlayerChangedDimensionEvent(player, fromDim, toDim))
    }

    fun firePlayerLoggedIn(player: Player?) {
        MinecraftForge.EVENT_BUS.post(PlayerLoggedInEvent(player))
    }

    fun firePlayerLoggedOut(player: Player?) {
        MinecraftForge.EVENT_BUS.post(PlayerLoggedOutEvent(player))
    }

    fun firePlayerRespawnEvent(player: Player?, endConquered: Boolean) {
        MinecraftForge.EVENT_BUS.post(PlayerRespawnEvent(player, endConquered))
    }

    fun firePlayerItemPickupEvent(player: Player?, item: ItemEntity?, clone: ItemStack?) {
        MinecraftForge.EVENT_BUS.post(ItemPickupEvent(player, item, clone))
    }

    fun firePlayerCraftingEvent(player: Player?, crafted: ItemStack?, craftMatrix: Container?) {
        MinecraftForge.EVENT_BUS.post(ItemCraftedEvent(player, crafted, craftMatrix))
    }

    fun firePlayerSmeltedEvent(player: Player?, smelted: ItemStack?) {
        MinecraftForge.EVENT_BUS.post(ItemSmeltedEvent(player, smelted))
    }

    fun onRenderTickStart(timer: Float) {
        MinecraftForge.EVENT_BUS.post(RenderTickEvent(TickEvent.Phase.START, timer))
    }

    fun onRenderTickEnd(timer: Float) {
        MinecraftForge.EVENT_BUS.post(RenderTickEvent(TickEvent.Phase.END, timer))
    }

    fun onPlayerPreTick(player: Player?) {
        MinecraftForge.EVENT_BUS.post(PlayerTickEvent(TickEvent.Phase.START, player))
    }

    fun onPlayerPostTick(player: Player?) {
        MinecraftForge.EVENT_BUS.post(PlayerTickEvent(TickEvent.Phase.END, player))
    }

    fun onPreLevelTick(level: Level?, haveTime: BooleanSupplier?) {
        MinecraftForge.EVENT_BUS.post(LevelTickEvent(LogicalSide.SERVER, TickEvent.Phase.START, level, haveTime))
    }

    fun onPostLevelTick(level: Level?, haveTime: BooleanSupplier?) {
        MinecraftForge.EVENT_BUS.post(LevelTickEvent(LogicalSide.SERVER, TickEvent.Phase.END, level, haveTime))
    }

    fun onPreClientTick() {
        MinecraftForge.EVENT_BUS.post(ClientTickEvent(TickEvent.Phase.START))
    }

    fun onPostClientTick() {
        MinecraftForge.EVENT_BUS.post(ClientTickEvent(TickEvent.Phase.END))
    }

    fun onPreServerTick(haveTime: BooleanSupplier?, server: MinecraftServer?) {
        MinecraftForge.EVENT_BUS.post(ServerTickEvent(TickEvent.Phase.START, haveTime, server))
    }

    fun onPostServerTick(haveTime: BooleanSupplier?, server: MinecraftServer?) {
        MinecraftForge.EVENT_BUS.post(ServerTickEvent(TickEvent.Phase.END, haveTime, server))
    }

    fun getPotentialSpawns(
        level: LevelAccessor?,
        category: MobCategory?,
        pos: BlockPos?,
        oldList: WeightedRandomList<SpawnerData?>?
    ): WeightedRandomList<SpawnerData?>? {
        val event: LevelEvent.PotentialSpawns = PotentialSpawns(level, category, pos, oldList)
        return if (MinecraftForge.EVENT_BUS.post(event)) WeightedRandomList.create() else WeightedRandomList.create(
            event.getSpawnerDataList()
        )
    }

    @ApiStatus.Internal
    fun onAdvancementEarnedEvent(player: Player?, earned: Advancement?) {
        MinecraftForge.EVENT_BUS.post(AdvancementEarnEvent(player, earned))
    }

    @ApiStatus.Internal
    fun onAdvancementProgressedEvent(
        player: Player?,
        progressed: Advancement?,
        advancementProgress: AdvancementProgress?,
        criterion: String?,
        progressType: ProgressType?
    ) {
        MinecraftForge.EVENT_BUS.post(
            AdvancementProgressEvent(
                player,
                progressed,
                advancementProgress,
                criterion,
                progressType
            )
        )
    }
}