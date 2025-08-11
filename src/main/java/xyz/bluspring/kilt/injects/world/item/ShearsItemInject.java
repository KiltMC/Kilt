package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.IForgeShearable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShearsItem.class)
public abstract class ShearsItemInject extends Item {
    public ShearsItemInject(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (interactionTarget instanceof IForgeShearable target) {
            if (interactionTarget.level().isClientSide())
                return InteractionResult.SUCCESS;

            var pos = BlockPos.containing(interactionTarget.position());

            if (target.isShearable(stack, interactionTarget.level(), pos)) {
                var drops = target.onSheared(player, stack, interactionTarget.level(), pos, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack));
                var random = RandomSource.create();

                drops.forEach(d -> {
                    var entity = interactionTarget.spawnAtLocation(d, 1f);
                    entity.setDeltaMovement(entity.getDeltaMovement().add((double) (random.nextFloat() - random.nextFloat()) * 0.1f, (double) (random.nextFloat() - random.nextFloat()) * 0.05f, (double) (random.nextFloat() - random.nextFloat()) * 0.1f));
                });

                stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(usedHand));
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
