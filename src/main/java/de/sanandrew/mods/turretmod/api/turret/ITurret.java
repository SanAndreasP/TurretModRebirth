/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.api.turret;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

@SuppressWarnings({"SameReturnValue", "unused"})
public interface ITurret
{
    String getName();

    @Nonnull
    UUID getId();

    ResourceLocation getItemModel();

    default void entityInit(ITurretInst turretInst) { }

    default void applyEntityAttributes(ITurretInst turretInst) { }

    ResourceLocation getStandardTexture(ITurretInst turretInst);

    ResourceLocation getGlowTexture(ITurretInst turretInst);

    SoundEvent getShootSound(ITurretInst turretInst);

    AxisAlignedBB getRangeBB(ITurretInst turretInst);

    int getTier();

    ITurretInfo getInfo();

    default void onUpdate(ITurretInst turretInst) { }

    default void writeSpawnData(ITurretInst turretInst, ByteBuf buf) { }

    default void readSpawnData(ITurretInst turretInst, ByteBuf buf) { }

    default void writeSyncData(ITurretInst turretInst, ObjectOutputStream stream) throws IOException { }

    default void readSyncData(ITurretInst turretInst, ObjectInputStream stream) throws IOException { }

    default void onSave(ITurretInst turretInst, NBTTagCompound nbt) { }

    default void onLoad(ITurretInst turretInst, NBTTagCompound nbt) { }

    default float getDeactiveHeadPitch() {
        return 30.0F;
    }

    default boolean canSeeThroughBlocks() {
        return false;
    }

    default SoundEvent getHurtSound(ITurretInst turretInst) {
        return null;
    }

    default SoundEvent getDeathSound(ITurretInst turretInst) {
        return null;
    }

    default SoundEvent getNoAmmoSound(ITurretInst turretInst) {
        return null;
    }

    default SoundEvent getCollectSound(ITurretInst turretInst) {
        return null;
    }
}
