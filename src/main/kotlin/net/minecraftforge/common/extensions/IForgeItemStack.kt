package net.minecraftforge.common.extensions

import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.EnderMan
import net.minecraft.world.entity.player.Player
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.pattern.BlockInWorld
import net.minecraft.world.phys.AABB
import net.minecraftforge.common.ToolAction
import net.minecraftforge.common.capabilities.ICapabilitySerializable

interface IForgeItemStack : ICapabilitySerializable<CompoundTag> {
    private fun self(): ItemStack {
        return this as ItemStack
    }
    
    private fun item(): IForgeItem {
        return (self().item as IForgeItem)
    }

    fun getCraftingRemainingItem(): ItemStack? {
        return item().getCraftingRemainingItem(self())
    }

    fun hasCraftingRemainingItem(): Boolean {
        return item().hasCraftingRemainingItem(self())
    }

    fun getBurnTime(recipeType: RecipeType<*>): Int {
        return item().getBurnTime(self(), recipeType)
    }

    fun onItemUseFirst(context: UseOnContext): InteractionResult? {
        val player = context.player
        val pos = context.clickedPos
        val state = BlockInWorld(context.level, pos, false)
        val registry = player?.level?.registryAccess()?.registryOrThrow(Registry.BLOCK_REGISTRY)

        return if (player != null &&
            registry != null &&
            !player.abilities.mayBuild &&
            !self().hasAdventureModePlaceTagForBlock(
                registry,
                state
            )
        ) {
            InteractionResult.PASS
        } else {
            val item = item()
            val result = item.onItemUseFirst(self(), context)
            if (player != null && result == InteractionResult.SUCCESS) {
                player.awardStat(Stats.ITEM_USED[self().item])
            }

            result
        }
    }

    override fun serializeNBT(): CompoundTag {
        val ret = CompoundTag()
        self().save(ret)
        return ret
    }

    fun canPerformAction(toolAction: ToolAction): Boolean {
        return item().canPerformAction(self(), toolAction)
    }

    fun onBlockStartBreak(pos: BlockPos, player: Player): Boolean {
        return !self().isEmpty && item().onBlockStartBreak(self(), pos, player)
    }

    fun shouldCauseBlockBreakReset(newStack: ItemStack): Boolean {
        return item().shouldCauseBlockBreakReset(self(), newStack)
    }

    fun canApplyAtEnchantingTable(enchantment: Enchantment): Boolean {
        return item().canApplyAtEnchantingTable(self(), enchantment)
    }

    fun getEnchantmentLevel(enchantment: Enchantment): Int {
        return item().getEnchantmentLevel(self(), enchantment)
    }

    fun getAllEnchantments(): Map<Enchantment, Int> {
        return item().getAllEnchantments(self())
    }

    fun getEnchantmentValue(): Int {
        return item().getEnchantmentValue(self())
    }

    fun getEquipmentSlot(): EquipmentSlot? {
        return item().getEquipmentSlot(self())
    }

    fun canDisableShield(shield: ItemStack, entity: LivingEntity, attacker: LivingEntity): Boolean {
        return item().canDisableShield(self(), shield, entity, attacker)
    }

    fun onEntitySwing(entity: LivingEntity): Boolean {
        return item().onEntitySwing(self(), entity)
    }

    fun onUsingTick(player: LivingEntity, count: Int) {
        item().onUsingTick(self(), player, count)
    }

    fun getEntityLifespan(level: Level): Int {
        return item().getEntityLifespan(self(), level)
    }

    fun onEntityItemUpdate(entity: ItemEntity): Boolean {
        return item().onEntityItemUpdate(self(), entity)
    }

    fun getXpRepairRatio(): Float {
        return item().getXpRepairRatio(self())
    }

    fun onArmorTick(level: Level, player: Player) {
        item().onArmorTick(self(), level, player)
    }

    fun onHorseArmorTick(level: Level, horse: Mob) {
        item().onHorseArmorTick(self(), level, horse)
    }

    fun canEquip(armorType: EquipmentSlot, entity: Entity): Boolean {
        return item().canEquip(self(), armorType, entity)
    }

    fun isBookEnchantable(book: ItemStack): Boolean {
        return item().isBookEnchantable(self(), book)
    }

    fun onDroppedByPlayer(player: Player): Boolean {
        return item().onDroppedByPlayer(self(), player)
    }

    fun getHighlightTip(displayName: Component): Component? {
        return item().getHighlightTip(self(), displayName)
    }

    fun getShareTag(): CompoundTag? {
        return item().getShareTag(self())
    }

    /**
     * Override this method to decide what to do with the NBT data received from
     * getNBTShareTag().
     *
     * @param nbt   Received NBT, can be null
     */
    fun readShareTag(nbt: CompoundTag?) {
        item().readShareTag(self(), nbt)
    }

    fun doesSneakBypassUse(level: LevelReader, pos: BlockPos, player: Player): Boolean {
        return self().isEmpty || item().doesSneakBypassUse(self(), level, pos, player)
    }

    fun areShareTagsEqual(other: ItemStack): Boolean {
        val shareTagA = this.getShareTag()
        val shareTagB = (other as IForgeItemStack).getShareTag()
        return if (shareTagA == null)
            shareTagB == null
        else
            shareTagB != null && shareTagA == shareTagB
    }

    fun equals(other: ItemStack, limitTags: Boolean): Boolean {
        return if (self().isEmpty)
            other.isEmpty
        else
            !other.isEmpty && self().count == other.count && item() === other.item &&
                if (limitTags)
                    this.areShareTagsEqual(other)
                else
                    ItemStack.tagMatches(self(), other)
    }

    fun isRepairable(): Boolean {
        return item().isRepairable(self())
    }

    fun isPiglinCurrency(): Boolean {
        return item().isPiglinCurrency(self())
    }

    fun makesPiglinsNeutral(wearer: LivingEntity): Boolean {
        return item().makesPiglinsNeutral(self(), wearer)
    }

    fun isEnderMask(player: Player, endermanEntity: EnderMan): Boolean {
        return item().isEnderMask(self(), player, endermanEntity)
    }

    fun canElytraFly(entity: LivingEntity): Boolean {
        return item().canElytraFly(self(), entity)
    }

    fun elytraFlightTick(entity: LivingEntity, flightTicks: Int): Boolean {
        return item().elytraFlightTick(self(), entity, flightTicks)
    }

    fun canWalkOnPowderedSnow(wearer: LivingEntity): Boolean {
        return item().canWalkOnPowderedSnow(self(), wearer)
    }

    fun getSweepHitBox(player: Player, target: Entity): AABB {
        return item().getSweepHitBox(self(), player, target)
    }

    fun onDestroyed(itemEntity: ItemEntity, damageSource: DamageSource) {
        item().onDestroyed(itemEntity, damageSource)
    }

    fun getFoodProperties(entity: LivingEntity): FoodProperties? {
        return item().getFoodProperties(self(), entity)
    }

    fun isNotReplaceableByPickAction(player: Player, inventorySlot: Int): Boolean {
        return item().isNotReplaceableByPickAction(self(), player, inventorySlot)
    }
}