package xyz.bluspring.kilt.injects.world.item;

import java.util.function.Function;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.StupidWorkarounds;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.ItemInjection;
import xyz.bluspring.kilt.injections.world.item.ItemPropertiesInjection;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@Mixin(Item.class)
public abstract class ItemInject implements IItemExtension, ItemInjection {
    @Unique private @Nullable ItemResource defaultResource;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$setCombineRepairProperty(Item.Properties properties, CallbackInfo ci) {
        this.canCombineRepair = properties.kilt$canCombineRepair();
    }

    @Override
    public void resetDefaultResource() {
        this.defaultResource = null;
    }

    @Override
    public ItemResource computeDefaultResource(Function<Item, ItemResource> resourceConstructor) {
        if (this.defaultResource == null)
            this.defaultResource = resourceConstructor.apply((Item) (Object) this);

        return this.defaultResource;
    }

    @ModifyReturnValue(method = "useOnRelease", at = @At("RETURN"))
    private boolean kilt$checkIsCrossbow(boolean original, @Local(argsOnly = true, name = "itemStack") ItemStack stack) {
        return original || stack.getItem() == Items.CROSSBOW;
    }

    @Unique protected boolean canCombineRepair;

    @Override
    public boolean isCombineRepairable(ItemStack stack) {
        return canCombineRepair && isDamageable(stack);
    }

    @Mixin(Item.Properties.class)
    public abstract static class PropertiesInject implements ItemPropertiesInjection {
        @Unique private boolean canCombineRepair = true;

        @Override
        public Item.Properties setNoCombineRepair() {
            this.canCombineRepair = false;
            return (Item.Properties) (Object) this;
        }

        @Override
        public boolean kilt$canCombineRepair() {
            return this.canCombineRepair;
        }
    }

    @Mixin(Item.TooltipContext.class)
    public interface TooltipContextInject extends TooltipContextInjection {
        @Shadow
        static Item.TooltipContext of(@Nullable Level level) {
            throw new UnsupportedOperationException("Implemented via mixin");
        }

        @Override
        @Nullable
        default Level level() {
            return null;
        }

        @Override
        @Nullable
        default Player player() {
            return null;
        }

        @CreateStatic
        private static Item.TooltipContext of(@Nullable Level pLevel, @Nullable Player player) {
            StupidWorkarounds.kilt$playerRef.set(player);
            var context = of(pLevel);
            StupidWorkarounds.kilt$playerRef.remove();

            return context;
        }

        @ModifyVariable(method = "of(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/item/Item$TooltipContext;", at = @At("HEAD"), argsOnly = true, name = "level")
        private static Level kilt$usePlayerLevelIfPossible(Level level) {
            var player = StupidWorkarounds.kilt$playerRef.get();
            if (level == null && player != null)
                return player.level();

            return level;
        }

        @Mixin(targets = "net/minecraft/world/item/Item$TooltipContext$2")
        abstract class AnonymousOfLevelInject implements TooltipContextInjection {
            @Shadow @Final Level val$level;
            @Unique private final Player val$player = StupidWorkarounds.kilt$playerRef.get();

            @Override
            public Level level() {
                return val$level;
            }

            @Override
            public Player player() {
                return val$player;
            }
        }
    }
}
