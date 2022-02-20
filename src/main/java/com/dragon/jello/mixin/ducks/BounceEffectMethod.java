package com.dragon.jello.mixin.ducks;

import com.dragon.jello.common.effects.JelloStatusEffectsRegistry;
import com.dragon.jello.lib.events.LivingEntityTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface BounceEffectMethod extends LivingEntityTickEvents.StartTick {

    Logger LOGGER = LogManager.getLogger("BOUNCE");

    default void onStartTick(LivingEntity livingEntity){
        bounce(livingEntity);
    }

    static void bounce(LivingEntity livingEntity) {
        World world = livingEntity.getWorld();

        Vec3d vec3d = livingEntity.getVelocity();
        if(livingEntity.hasStatusEffect(JelloStatusEffectsRegistry.BOUNCE)) {
            if (vec3d.y < 0.0 && livingEntity.isOnGround()) {
                double d = 1.0; //entity instanceof LivingEntity ? 1.0 : 0.8;

                Vec3d rotationVector = livingEntity.getRotationVector();
                double horizontalVel = vec3d.horizontalLength();

                if (livingEntity.isPlayer() && world.getTime() % 20 == 0 /* && !world.isClient */) {
                    LOGGER.info(world.getTime());
                    LOGGER.info("1: Player's Rotation Vec: [" + rotationVector + "]");
                    LOGGER.info("2: Player's Velocity: [" + vec3d + "]");
                    LOGGER.info("3: Player's Horizontal Velocity: [" + horizontalVel + "]");
                }

                //double xzMultiplier = horizontalVel / 2000;

                livingEntity.setVelocity(vec3d.x, -vec3d.y * d, vec3d.z);

                livingEntity.setOnGround(false);

                //livingEntity.setVelocity(vec3d.x * Math.min(1.5, xzMultiplier), (-1 * vec3d.y /* / Math.min(1.0, xzMultiplier)*/), vec3d.z * Math.min(1.5, xzMultiplier));
            }
        }

    }
}
