package xyz.bluspring.kilt.forgeinjects.world.entity.living;

import io.github.fabricators_of_create.porting_lib.extensions.EntityExtensions;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.EntityEquipmentInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityInject extends Entity implements IForgeLivingEntity, EntityExtensions, CapabilityProviderInjection {
    public LivingEntityInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract boolean isAlive();

    private LazyOptional<?>[] handlers = EntityEquipmentInvWrapper.create((LivingEntity) (Object) this);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (this.isAlive() && cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null)
                return handlers[2].cast();
            else if (side.getAxis().isVertical())
                return handlers[0].cast();
            else if (side.getAxis().isHorizontal())
                return handlers[1].cast();
        }

        return this.getCapability(cap, side);
    }

    @Redirect(method = "dropFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V"))
    public void kilt$disableVanillaLootTable(LootTable instance, LootContext lootContext, Consumer<ItemStack> consumer) {
    }

    @Inject(at = @At("TAIL"), method = "dropFromLootTable", locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$useForgeLootTables(DamageSource damageSource, boolean bl, CallbackInfo ci, ResourceLocation resourceLocation, LootTable lootTable, LootContext.Builder builder) {
        var ctx = builder.create(LootContextParamSets.ENTITY);
        lootTable.getRandomItems(ctx).forEach(((LivingEntity) (Object) this)::spawnAtLocation);
    }
}
