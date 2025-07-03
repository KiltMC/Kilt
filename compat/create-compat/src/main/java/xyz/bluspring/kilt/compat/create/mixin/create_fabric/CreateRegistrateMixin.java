package xyz.bluspring.kilt.compat.create.mixin.create_fabric;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.compat.create.mixin.registrate_fabric.AbstractRegistrateMixin;

@IfModLoaded("create")
@Mixin(CreateRegistrate.class)
public abstract class CreateRegistrateMixin extends AbstractRegistrateMixin<CreateRegistrate> {
    @Shadow @Nullable protected ResourceKey<CreativeModeTab> currentTab;

    @Unique
    public CreateRegistrate registerEventListeners(IEventBus bus) {
        return super.registerEventListeners(bus);
    }

    public CreateRegistrate setCreativeTab(RegistryObject<CreativeModeTab> tab) {
        this.currentTab = tab.getKey();
        return (CreateRegistrate) (Object) this;
    }
}
