package xyz.bluspring.kilt.forgeinjects.world.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.extensions.IForgePlayer;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.entity.PlayerInjection;

@Mixin(Player.class)
public class PlayerInject implements IForgePlayer, PlayerInjection {
}
