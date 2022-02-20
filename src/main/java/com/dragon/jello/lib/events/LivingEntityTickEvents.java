package com.dragon.jello.lib.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;

public class LivingEntityTickEvents {

    public static final Event<StartTick> START_TICK = EventFactory.createArrayBacked(StartTick.class,
        (callbacks) -> (livingEntity) -> {
        for (StartTick event : callbacks) {
            event.onStartTick(livingEntity);
        }
    });

    @FunctionalInterface
    public interface StartTick {
        void onStartTick(LivingEntity livingEntity);
    }
}
