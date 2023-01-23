package xyz.bluspring.kilt.remaps.world.entity.player

import com.mojang.authlib.GameProfile
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.ProfilePublicKey
import net.minecraft.world.level.Level

abstract class PlayerRemap(level: Level, blockPos: BlockPos, f: Float, gameProfile: GameProfile, profilePublicKey: ProfilePublicKey?) : Player(level, blockPos, f, gameProfile, profilePublicKey) {
    companion object {
        const val PERSISTED_NBT_TAG = "PlayerPersisted"
    }
}