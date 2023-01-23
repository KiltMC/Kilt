/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PlayerContainerEvent extends PlayerEvent
{
    private final AbstractContainerMenu container;

    public PlayerContainerEvent() {
        container = null;
    }

    public PlayerContainerEvent(Player player, AbstractContainerMenu container)
    {
        super(player);
        this.container = container;
    }

    public static class Open extends PlayerContainerEvent
    {
        public Open() {}
        public Open(Player player, AbstractContainerMenu container)
        {
            super(player, container);
        }
    }
    public static class Close extends PlayerContainerEvent
    {
        public Close() {}
        public Close(Player player, AbstractContainerMenu container)
        {
            super(player, container);
        }
    }

    public AbstractContainerMenu getContainer()
    {
        return container;
    }
}
