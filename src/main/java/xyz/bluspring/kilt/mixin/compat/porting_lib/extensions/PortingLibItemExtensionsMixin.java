//package xyz.bluspring.kilt.mixin.compat.porting_lib.extensions;
//
//import io.github.fabricators_of_create.porting_lib.enchant.CustomEnchantingBehaviorItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.ArmorTextureItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.ArmorTickListeningItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.BlockBreakResetItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.BlockUseBypassingItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.ContinueUsingItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.CreativeModeTabExt;
//import io.github.fabricators_of_create.porting_lib.item.extensions.CreatorModIdItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.CustomArrowItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.CustomDamageItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.CustomEnchantmentValueItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.CustomFuelItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.CustomMapItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.CustomPickupBucketSoundItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.CustomSupportsEnchantItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.DamageableItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.EnderMaskItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.EntitySwingListenerItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.EntityTickListenerItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.EquipmentItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.InfiniteArrowItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.OnDestroyedItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.PiglinsNeutralItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.ReequipAnimationItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.ShieldBlockItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.SneakBypassUseItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.UseFirstBehaviorItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.UsingTickItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.WalkOnSnowItem;
//import io.github.fabricators_of_create.porting_lib.item.extensions.XpRepairItem;
//import io.github.fabricators_of_create.porting_lib.tool.addons.ItemAbilityItem;
//import net.neoforged.neoforge.common.extensions.IItemExtension;
//import org.spongepowered.asm.mixin.Mixin;
//
//@Mixin(value = {
//    // base module
//    CustomEnchantingBehaviorItem.class,
//    io.github.fabricators_of_create.porting_lib.item.DamageableItem.class,
//
//    // item module
//    ArmorTextureItem.class,
//    ArmorTickListeningItem.class,
//    BlockBreakResetItem.class,
//    BlockUseBypassingItem.class,
//    ContinueUsingItem.class,
//    CreativeModeTabExt.class,
//    CreatorModIdItem.class,
//    CustomArrowItem.class,
//    CustomDamageItem.class,
//    CustomEnchantmentValueItem.class,
//    CustomFuelItem.class,
//    CustomMapItem.class,
//    CustomPickupBucketSoundItem.class,
//    CustomSupportsEnchantItem.class,
//    DamageableItem.class,
//    EnderMaskItem.class,
//    EntitySwingListenerItem.class,
//    EntityTickListenerItem.class,
//    EquipmentItem.class,
//    InfiniteArrowItem.class,
//    OnDestroyedItem.class,
//    PiglinsNeutralItem.class,
//    ReequipAnimationItem.class,
//    ShieldBlockItem.class,
//    SneakBypassUseItem.class,
//    UseFirstBehaviorItem.class,
//    UsingTickItem.class,
//    WalkOnSnowItem.class,
//    XpRepairItem.class,
//
//    // item abilities module
//    ItemAbilityItem.class,
//})
//public interface PortingLibItemExtensionsMixin extends IItemExtension {
//}
