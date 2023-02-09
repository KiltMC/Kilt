/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.event.entity;

import java.util.Map;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import xyz.bluspring.kilt.Kilt;

/**
 * EntityAttributeCreationEvent.<br>
 * Use this event to register attributes for your own EntityTypes.
 * This event is fired after registration and before common setup.
 * <br>
 * Fired on the Mod bus {@link IModBusEvent}.<br>
 **/
public class EntityAttributeCreationEvent extends Event implements IModBusEvent
{
    private final Map<EntityType<? extends LivingEntity>, AttributeSupplier> map;

    public EntityAttributeCreationEvent() {
        map = null;
    }

    public EntityAttributeCreationEvent(Map<EntityType<? extends LivingEntity>, AttributeSupplier> map)
    {
        this.map = map;
    }

    public void put(EntityType<? extends LivingEntity> entity, AttributeSupplier map)
    {
        if (DefaultAttributes.hasSupplier(entity)) {
            Kilt.Companion.getLogger().warn("Duplicate DefaultAttributes entry: " + entity);
            Kilt.Companion.getLogger().warn("This will be ignored so Minecraft doesn't throw a fit.");

            return;
        }
        this.map.put(entity, map);
    }
}
