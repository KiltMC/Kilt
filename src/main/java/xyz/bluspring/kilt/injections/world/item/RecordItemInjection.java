package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public interface RecordItemInjection {
    static RecordItem create(int analogOutput, Supplier<SoundEvent> soundSupplier, Item.Properties properties, int lengthInSeconds) {
        try {
            var recordInit = RecordItem.class.getDeclaredConstructor(int.class, Supplier.class, Item.Properties.class, int.class);
            return recordInit.newInstance(analogOutput, soundSupplier, properties, lengthInSeconds);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
