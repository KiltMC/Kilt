package xyz.bluspring.kilt.injections.client;

import com.mojang.authlib.properties.PropertyMap;

public interface UserInjection {
    void setProperties(PropertyMap properties);
    boolean hasCachedProperties();
}
