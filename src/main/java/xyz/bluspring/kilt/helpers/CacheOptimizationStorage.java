package xyz.bluspring.kilt.helpers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import kotlin.Pair;
import kotlin.Triple;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.util.Quadruple;

import java.time.Duration;

public class CacheOptimizationStorage {
    public static final Cache<Quadruple<IClientItemExtensions, LivingEntity, ItemStack, EquipmentSlot>, HumanoidModel<?>> ARMOR_MODEL_CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(5L))
        .build();
}
