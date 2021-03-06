/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.entity.turret;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

import java.util.HashMap;
import java.util.Map;

public final class DataWatcherBooleans<T extends Entity>
{
    private final T entity;
    private final DataParameter<Integer> param;
    private static final Map<Class<? extends Entity>, DataParameter<Integer>> PARAMS = new HashMap<>();

    DataWatcherBooleans(T e) {
        this.entity = e;
        Class<? extends Entity> entityCls = e.getClass();
        if( !PARAMS.containsKey(entityCls) ) {
            PARAMS.put(entityCls, EntityDataManager.createKey(e.getClass(), DataSerializers.VARINT));
        }
        this.param = PARAMS.get(entityCls);
    }

    void registerDwValue() {
        this.entity.getDataManager().register(this.param, 0);
    }

    void setBit(int bit, boolean value) {
        int dwVal = this.entity.getDataManager().get(this.param);
        if( value ) {
            dwVal = dwVal | (1 << bit);
        } else {
            dwVal = dwVal & ~( 1 << bit );
        }
        this.entity.getDataManager().set(this.param, dwVal);
    }

    boolean getBit(int bit) {
        return (((this.entity.getDataManager().get(this.param) & (1 << bit)) >> bit) & 1) == 1;
    }

    void writeToNbt(NBTTagCompound nbt) {
        nbt.setInteger("dataWatcherBools", this.entity.getDataManager().get(this.param));
    }

    void readFromNbt(NBTTagCompound nbt) {
        this.entity.getDataManager().set(this.param, nbt.getInteger("dataWatcherBools"));
    }

    public enum Turret {
        ACTIVE(0);

        public final int bit;

        Turret(int bit) {
            this.bit = bit;
        }
    }
}
