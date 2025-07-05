package xyz.bluspring.kilt.helpers;

import com.mojang.blaze3d.audio.Channel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SoundConsumerStorage {
    // The sound engine is lambda hell, and so we have to use this storage to be able to ensure that the channel access execute inject in
    // ChannelAccessHandleMixin will actually be run in the correct places. We also can't wrap the consumer, because otherwise,
    // some other mod that tries to do the same thing will cause either us or them to fail.
    public static final Set<Consumer<Channel>> soundConsumerChannels = Collections.synchronizedSet(new HashSet<>());
}
