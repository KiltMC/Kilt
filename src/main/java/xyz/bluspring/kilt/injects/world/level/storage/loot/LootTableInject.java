package xyz.bluspring.kilt.injects.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.fabricators_of_create.porting_lib.loot.extensions.LootTableExtensions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.storage.loot.LootPoolInjection;
import xyz.bluspring.kilt.injections.world.level.storage.loot.LootTableInjection;

import java.util.List;
import java.util.Optional;

@Mixin(LootTable.class)
public abstract class LootTableInject implements LootTableInjection, LootTableExtensions {
    @Shadow @Final @Mutable
    private List<LootPool> pools;
    @Unique private boolean isFrozen = false;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$createPoolsList(LootContextParamSet paramSet, Optional<ResourceLocation> randomSequence, List<LootPool> pools, List<LootItemFunction> functions, CallbackInfo ci) {
        this.pools = Lists.newArrayList(this.pools);
    }

    @ModifyReturnValue(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    private ObjectArrayList<ItemStack> kilt$modifyLootTable(ObjectArrayList<ItemStack> original, @Local(argsOnly = true) LootContext context) {
        return CommonHooks.modifyLoot(this.getLootTableId(), original, context);
    }

    @Override
    public void freeze() {
        this.isFrozen = true;
        this.pools.forEach(LootPoolInjection::freeze);
    }

    @Override
    public boolean isFrozen() {
        return isFrozen;
    }

    private void checkFrozen() {
        if (this.isFrozen())
            throw new RuntimeException("Attempted to modify LootTable after being finalized!");
    }

    @Override
    public LootPool getPool(String name) {
        return this.pools.stream().filter(e -> name.equals(e.getName())).findFirst().orElse(null);
    }

    @Override
    public LootPool removePool(String name) {
        checkFrozen();
        for (LootPool pool : this.pools) {
            if (name.equals(pool.getName())) {
                this.pools.remove(pool);
                return pool;
            }
        }
        return null;
    }

    @Override
    public void addPool(LootPool pool) {
        checkFrozen();
        if (this.pools.stream().anyMatch(e -> e == pool || e.getName() != null && e.getName().equals(pool.getName())))
            throw new RuntimeException("Attempted to add a duplicate pool to loot table: " + pool.getName());

        this.pools.add(pool);
    }
}
