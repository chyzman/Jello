package com.dragon.jello.mixin.mixins.common;

import com.dragon.jello.common.Util.DataConstants;
import com.dragon.jello.common.effects.JelloStatusEffectsRegistry;
import com.dragon.jello.lib.events.LivingEntityTickEvents;
import com.dragon.jello.mixin.ducks.DyeableEntity;
import com.dragon.jello.mixin.ducks.InAirTracking;
import com.dragon.jello.mixin.ducks.RainbowEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static com.dragon.jello.common.Util.DataConstants.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements DyeableEntity, RainbowEntity, InAirTracking {

    private boolean isInAir = false;

    private static final TrackedData<Integer> DYE_COLOR = DataConstants.DYE_COLOR;
    private static final TrackedData<Byte> RAINBOW_MODE = DataConstants.RAINBOW_MODE;
    private static final TrackedData<Integer> CONSTANT_COLOR = DataConstants.CONSTANT_COLOR;

    @Inject(method = "initDataTracker", at = @At(value = "TAIL"))
    private void initDyeColorTracker(CallbackInfo ci){
        ((LivingEntity) (Object)this).getDataTracker().startTracking(DYE_COLOR, 16);
        ((LivingEntity) (Object)this).getDataTracker().startTracking(RAINBOW_MODE, (byte)0);
        ((LivingEntity) (Object)this).getDataTracker().startTracking(CONSTANT_COLOR, DEFAULT_NULL_COLOR_VALUE);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
    public void writeDyeColorNBT(NbtCompound nbt, CallbackInfo ci){
        nbt.putInt(getDyeColorNbtKey(), ((LivingEntity) (Object)this).getDataTracker().get(DYE_COLOR));
        nbt.putByte(getRainbowNbtKey(), ((LivingEntity) (Object)this).getDataTracker().get(RAINBOW_MODE));
        nbt.putString(getConstantColorNbtKey(), ((LivingEntity) (Object)this).getDataTracker().get(CONSTANT_COLOR).toString());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At(value = "TAIL"))
    public void readDyeColorNBT(NbtCompound nbt, CallbackInfo ci){
        ((LivingEntity) (Object)this).getDataTracker().set(DYE_COLOR, getOrDefaultNbtInt(getDyeColorNbtKey(), nbt, 16));
        ((LivingEntity) (Object)this).getDataTracker().set(RAINBOW_MODE, nbt.getByte(getRainbowNbtKey()));
        ((LivingEntity) (Object)this).getDataTracker().set(CONSTANT_COLOR, getOrDefaultNbtColor(getConstantColorNbtKey(), nbt, DEFAULT_NULL_COLOR_VALUE));
    }

    private int getOrDefaultNbtInt(String key, NbtCompound nbt, int defaultValue){
        return nbt.contains(key) ? nbt.getInt(key) : defaultValue;
    }

    private Integer getOrDefaultNbtColor(String key, NbtCompound nbt, int defaultValue){
        if(nbt.contains(key)){
            String string = nbt.getString(key);
            Integer colorValue = null;

            if(string.startsWith("#")){
                String hexValue = string.replace('#', ' ').trim();

                try{
                    colorValue = Integer.parseInt(hexValue, 16);
                } catch (NumberFormatException ignore) {}

                if(colorValue != null){
                    return colorValue;
                }
            }else{
                try{
                    colorValue = Integer.parseInt(string, 10);
                } catch (NumberFormatException ignore) {}

                if(colorValue != null){
                    return colorValue;
                }
            }
        }

        return defaultValue;

    }

    //---------------------------------------------------------------------------------------------------//
    @Override
    public int getDyeColorID() {
        return ((LivingEntity) (Object)this).getDataTracker().get(DYE_COLOR);
    }

    @Override
    public void setDyeColorID(int dyeColorID){
        ((LivingEntity) (Object)this).getDataTracker().set(DYE_COLOR, dyeColorID);
    }

    @Override
    public boolean dyeColorOverride(){
        return false;
    }

    //---------------------------------------------------------------------------------------------------//
    @Override
    public void setRainbowTime(boolean value){
        ((LivingEntity) (Object)this).getDataTracker().set(RAINBOW_MODE, value ? (byte) 1 : 0);
    }

    @Override
    public boolean isRainbowTime(){
        return rainbowOverride() || ((LivingEntity) (Object) this).getDataTracker().get(RAINBOW_MODE) == 1;
    }

    @Override
    public boolean rainbowOverride(){
        return false;
    }

    //---------------------------------------------------------------------------------------------------//

    @Override
    public int getConstantColor() {
        return ((LivingEntity) (Object)this).getDataTracker().get(CONSTANT_COLOR);
    }

    //---------------------------------------------------------------------------------------------------//

    private int timeLeftBeforeNextJump = 0;

    @Unique
    private static final Logger BOUNCE_LOGGER = LogManager.getLogger("BOUNCE");

    @Inject(method = "jump", at = @At(value = "HEAD"), cancellable = true)
    private void setJumpVariable(CallbackInfo ci){
        LivingEntity livingEntity = (LivingEntity) (Object)this;
        //isInAir = true;

        Vec3d rotationVector = livingEntity.getRotationVector();

        if(rotationVector.getY() < 0){
            return;
        }

        if (livingEntity.hasStatusEffect(JelloStatusEffectsRegistry.BOUNCE) && !isInAir) {
            float f = 3.2f;


            BOUNCE_LOGGER.info("1: Player's Rotation Vec: [" + rotationVector + "]");
            BOUNCE_LOGGER.info("2: Player's Velocity: [" + livingEntity.getVelocity() + "]");

            if(Math.abs(rotationVector.y) < 0.3){
                rotationVector = new Vec3d(rotationVector.x, 0.45F, rotationVector.z);
            }

            Vec3d vec = livingEntity.getVelocity().add(rotationVector);

            livingEntity.addVelocity(vec.x * f, vec.y * f / 3f, vec.z * f);

            setIsInAir(true);
            timeLeftBeforeNextJump = 60;

            livingEntity.velocityDirty = true;

            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void manageTimeLeftBetweenJumps(CallbackInfo ci){
        if(timeLeftBeforeNextJump <= 0){
            setIsInAir(false);
        }else{
            timeLeftBeforeNextJump--;
        }
    }

    @Unique
    public boolean isInAir(){
        return isInAir;
    }

    @Unique
    public void setIsInAir(boolean jumped){
        this.isInAir = jumped;
    }
}
