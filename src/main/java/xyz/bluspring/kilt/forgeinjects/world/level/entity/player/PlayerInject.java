package xyz.bluspring.kilt.forgeinjects.world.level.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.extensions.IForgePlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public class PlayerInject implements IForgePlayer {

}
