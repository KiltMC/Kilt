package net.minecraftforge.common.extensions

import com.google.common.collect.Multimap
import io.github.fabricators_of_create.porting_lib.extensions.ItemExtensions
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.EnderMan
import net.minecraft.world.entity.monster.piglin.PiglinAi
import net.minecraft.world.entity.player.Player
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.*
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.ToolAction
import net.minecraftforge.common.ToolActions
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.items.wrapper.ShulkerItemStackInvWrapper
import java.util.function.Consumer


interface IForgeItem : ItemExtensions, io.github.fabricators_of_create.porting_lib.extensions.tool.ItemExtensions {
    private fun self(): Item {
        return this as Item
    }

    fun getAttributeModifiers(slot: EquipmentSlot, stack: ItemStack): Multimap<Attribute?, AttributeModifier?>? {
        return self().getDefaultAttributeModifiers(slot)
    }

    fun onDroppedByPlayer(item: ItemStack, player: Player): Boolean {
        return true
    }

    fun getHighlightTip(item: ItemStack, displayName: Component): Component? {
        return displayName
    }

    fun onItemUseFirst(stack: ItemStack, context: UseOnContext): InteractionResult? {
        return InteractionResult.PASS
    }

    fun isPiglinCurrency(stack: ItemStack): Boolean {
        return stack.item === PiglinAi.BARTERING_ITEM
    }

    fun makesPiglinsNeutral(stack: ItemStack, wearer: LivingEntity): Boolean {
        return stack.item is ArmorItem && (stack.item as ArmorItem).material == ArmorMaterials.GOLD
    }

    fun isRepairable(stack: ItemStack): Boolean

    fun getXpRepairRatio(stack: ItemStack): Float {
        return 2f
    }

    fun getShareTag(stack: ItemStack): CompoundTag? {
        return stack.tag
    }

    fun readShareTag(stack: ItemStack, nbt: CompoundTag?) {
        stack.tag = nbt
    }
    fun onUsingTick(stack: ItemStack, player: LivingEntity, count: Int) {}

    fun getCraftingRemainingItem(itemStack: ItemStack): ItemStack? {
        return if (!hasCraftingRemainingItem(itemStack)) {
            ItemStack.EMPTY
        } else ItemStack(self().craftingRemainingItem!!)
    }

    fun hasCraftingRemainingItem(stack: ItemStack): Boolean {
        return self().hasCraftingRemainingItem()
    }

    fun getEntityLifespan(itemStack: ItemStack, level: Level): Int {
        return 6000
    }

    fun onEntityItemUpdate(stack: ItemStack, entity: ItemEntity): Boolean {
        return false
    }

    val creativeTabs: Collection<CreativeModeTab>
        get() = mutableListOf<CreativeModeTab>().apply {
            if (self().itemCategory != null)
                add(self().itemCategory!!)
        }.toList()

    fun doesSneakBypassUse(stack: ItemStack, level: LevelReader, pos: BlockPos, player: Player): Boolean {
        return false
    }

    fun onArmorTick(stack: ItemStack, level: Level, player: Player) {}

    fun canEquip(stack: ItemStack, armorType: EquipmentSlot, entity: Entity): Boolean {
        return Mob.getEquipmentSlotForItem(stack) == armorType
    }

    fun getEquipmentSlot(stack: ItemStack): EquipmentSlot? {
        return null
    }

    fun isBookEnchantable(stack: ItemStack, book: ItemStack): Boolean {
        return true
    }

    fun getArmorTexture(stack: ItemStack, entity: Entity, slot: EquipmentSlot, type: String): String? {
        return null
    }

    fun onEntitySwing(stack: ItemStack, entity: LivingEntity): Boolean {
        return false
    }

    fun getDamage(stack: ItemStack): Int {
        return if (!stack.hasTag())
            0
        else
            stack.tag!!.getInt("Damage")
    }

    /**
     * Return the maxDamage for this ItemStack. Defaults to the maxDamage field in
     * this item, but can be overridden here for other sources such as NBT.
     *
     * @param stack The itemstack that is damaged
     * @return the damage value
     */
    fun getMaxDamage(stack: ItemStack?): Int {
        return self().maxDamage
    }

    fun isDamaged(stack: ItemStack): Boolean {
        return stack.damageValue > 0
    }

    fun setDamage(stack: ItemStack, damage: Int) {
        stack.orCreateTag.putInt("Damage", 0.coerceAtLeast(damage))
    }

    fun canPerformAction(stack: ItemStack, toolAction: ToolAction): Boolean {
        return false
    }

    fun isCorrectToolForDrops(stack: ItemStack, state: BlockState): Boolean {
        return self().isCorrectToolForDrops(state)
    }

    fun getMaxStackSize(stack: ItemStack): Int {
        return self().maxStackSize
    }

    fun getEnchantmentValue(stack: ItemStack): Int {
        return self().enchantmentValue
    }

    fun canApplyAtEnchantingTable(stack: ItemStack, enchantment: Enchantment): Boolean {
        return enchantment.category.canEnchant(stack.item)
    }

    fun getEnchantmentLevel(stack: ItemStack, enchantment: Enchantment): Int {
        return EnchantmentHelper.getTagEnchantmentLevel(enchantment, stack)
    }

    fun getAllEnchantments(stack: ItemStack): Map<Enchantment, Int> {
        return EnchantmentHelper.deserializeEnchantments(stack.enchantmentTags)
    }

    fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean {
        return oldStack != newStack
    }

    fun shouldCauseBlockBreakReset(oldStack: ItemStack, newStack: ItemStack): Boolean {
        if (!newStack.`is`(oldStack.item))
            return true

        if (!newStack.isDamageableItem || !oldStack.isDamageableItem)
            return !ItemStack.tagMatches(newStack, oldStack)

        val newTag = newStack.tag
        val oldTag = oldStack.tag

        if (newTag == null || oldTag == null)
            return !(newTag == null && oldTag == null)

        val newKeys = mutableSetOf<String>().apply {
            addAll(newTag.allKeys)
        }
        val oldKeys = mutableSetOf<String>().apply {
            addAll(oldTag.allKeys)
        }

        newKeys.remove(ItemStack.TAG_DAMAGE)
        oldKeys.remove(ItemStack.TAG_DAMAGE)

        return if (newKeys != oldKeys) true else !newKeys.stream().allMatch {
            newTag[it] == oldTag[it]
        }
    }

    fun canContinueUsing(oldStack: ItemStack, newStack: ItemStack): Boolean {
        return ItemStack.isSameIgnoreDurability(oldStack, newStack)
    }

    fun initCapabilities(stack: ItemStack, nbt: CompoundTag?): ICapabilityProvider? {
        return ShulkerItemStackInvWrapper.createDefaultProvider(stack)
    }

    fun canDisableShield(
        stack: ItemStack,
        shield: ItemStack,
        entity: LivingEntity,
        attacker: LivingEntity
    ): Boolean {
        return this is AxeItem
    }

    fun getBurnTime(itemStack: ItemStack, recipeType: RecipeType<*>): Int {
        return -1
    }

    fun onHorseArmorTick(stack: ItemStack, level: Level, horse: Mob) {}

    fun <T : LivingEntity> damageItem(stack: ItemStack, amount: Int, entity: T, onBroken: Consumer<T>): Int {
        return amount
    }

    fun onDestroyed(itemEntity: ItemEntity, damageSource: DamageSource) {
        self().onDestroyed(itemEntity)
    }

    fun isEnderMask(stack: ItemStack, player: Player, endermanEntity: EnderMan): Boolean {
        return stack.item == Blocks.CARVED_PUMPKIN.asItem()
    }

    fun canElytraFly(stack: ItemStack, entity: LivingEntity): Boolean {
        return false
    }

    fun elytraFlightTick(stack: ItemStack, entity: LivingEntity, flightTicks: Int): Boolean {
        return false
    }

    fun canWalkOnPowderedSnow(stack: ItemStack, wearer: LivingEntity): Boolean {
        return stack.`is`(Items.LEATHER_BOOTS)
    }

    fun isDamageable(stack: ItemStack): Boolean {
        return self().canBeDepleted()
    }

    fun getSweepHitBox(stack: ItemStack, player: Player, target: Entity): AABB {
        return target.boundingBox.inflate(1.0, 0.25, 1.0)
    }

    fun getFoodProperties(stack: ItemStack, entity: LivingEntity?): FoodProperties? {
        return self().foodProperties
    }

    fun isNotReplaceableByPickAction(stack: ItemStack, player: Player, inventorySlot: Int): Boolean {
        return stack.isEnchanted
    }
}
