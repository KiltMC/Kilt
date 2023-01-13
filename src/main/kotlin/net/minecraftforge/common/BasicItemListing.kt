package net.minecraftforge.common

import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.trading.MerchantOffer

open class BasicItemListing(
    @JvmField protected val price: ItemStack,
    @JvmField protected val price2: ItemStack,
    @JvmField protected val forSale: ItemStack,
    @JvmField protected val maxTrades: Int,
    @JvmField protected val xp: Int,
    @JvmField protected val priceMult: Float
) : ItemListing {
    constructor(price: ItemStack, forSale: ItemStack, maxTrades: Int, xp: Int, priceMult: Float) : this(price, ItemStack.EMPTY, forSale, maxTrades, xp, priceMult)
    constructor(emeralds: Int, forSale: ItemStack, maxTrades: Int, xp: Int, mult: Float) : this(ItemStack(Items.EMERALD, emeralds), forSale, maxTrades, xp, mult)
    constructor(emeralds: Int, forSale: ItemStack, maxTrades: Int, xp: Int) : this(ItemStack(Items.EMERALD), forSale, maxTrades, xp, 1F)

    override fun getOffer(entity: Entity, randomSource: RandomSource): MerchantOffer? {
        return MerchantOffer(price, price2, forSale, maxTrades, xp, priceMult)
    }
}