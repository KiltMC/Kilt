package net.minecraftforge.common

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.item.SpawnEggItem
import java.util.function.Supplier

open class ForgeSpawnEggItem(private val type: Supplier<out EntityType<out Mob>>, backgroundColor: Int, highlightColor: Int, props: Properties)
: SpawnEggItem(
    type.get(), // Forge did this in a way where it just casts null to EntityType. That feels very illegal, let's not do that.
    backgroundColor, highlightColor, props
) {
    override fun getType(tag: CompoundTag?): EntityType<*> {
        return super.getType(tag) ?: this.type.get()
    }

    companion object {
        @JvmStatic
        fun fromEntityType(type: EntityType<*>?): SpawnEggItem? {
            return byId(type)
        }
    }
}