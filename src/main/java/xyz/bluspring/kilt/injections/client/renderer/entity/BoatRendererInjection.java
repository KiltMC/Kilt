package xyz.bluspring.kilt.injections.client.renderer.entity;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.ListModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

public interface BoatRendererInjection {
    Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat);
}
