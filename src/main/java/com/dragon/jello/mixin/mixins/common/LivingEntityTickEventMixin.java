package com.dragon.jello.mixin.mixins.common;

import com.dragon.jello.lib.events.LivingEntityTickEvents;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityTickEventMixin {

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onLivingEntityTick(CallbackInfo ci){
        LivingEntityTickEvents.START_TICK.invoker().onStartTick((LivingEntity)(Object)this);
    }
}
