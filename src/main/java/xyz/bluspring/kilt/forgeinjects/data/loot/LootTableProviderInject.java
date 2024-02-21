package xyz.bluspring.kilt.forgeinjects.data.loot;

import com.google.common.collect.Sets;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.LootTableProviderInjection;

import java.util.List;
import java.util.Map;

@Mixin(LootTableProvider.class)
public class LootTableProviderInject implements LootTableProviderInjection {
    @Shadow @Final private List<LootTableProvider.SubProviderEntry> subProviders;

    @Override
    public List<LootTableProvider.SubProviderEntry> getTables() {
        return this.subProviders;
    }

    @Override
    public void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationContext) {
        for (ResourceLocation location : Sets.difference(BuiltInLootTables.all(), map.keySet())) {
            validationContext.reportProblem("Missing built-in table: " + location);
        }

        map.forEach((location, table) -> {
            table.validate(validationContext.setParams(table.getParamSet()).enterElement("{" + location + "}", new LootDataId<>(LootDataType.TABLE, location)));
        });
    }
}
