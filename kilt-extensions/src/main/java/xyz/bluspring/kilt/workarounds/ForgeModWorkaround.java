package xyz.bluspring.kilt.workarounds;

import net.minecraftforge.registries.holdersets.*;
import net.minecraftforge.registries.holdersets.HolderSetType;

// This class is used to do something that for some reason Kotlin can't do.
// Unless I'm doing something wrong.
public class ForgeModWorkaround {
    private static final DeferredRegister<HolderSetType<?>> HOLDER_SET_TYPES = DeferredRegister.create(ForgeRegistries.Keys.HOLDER_SET_TYPES, "forge");
    public static RegistryObject<HolderSetType<?>> ANY_HOLDER_SET = HOLDER_SET_TYPES.register("any", () -> AnyHolderSet::codec);
    public static RegistryObject<HolderSetType<?>> AND_HOLDER_SET = HOLDER_SET_TYPES.register("and", () -> AndHolderSet::codec);
    public static RegistryObject<HolderSetType<?>> OR_HOLDER_SET = HOLDER_SET_TYPES.register("or", () -> OrHolderSet::codec);
    public static RegistryObject<HolderSetType<?>> NOT_HOLDER_SET = HOLDER_SET_TYPES.register("not", () -> NotHolderSet::codec);
}
