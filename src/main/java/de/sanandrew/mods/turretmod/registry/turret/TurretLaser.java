/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.registry.turret;

import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.turret.ITurret;
import de.sanandrew.mods.turretmod.api.turret.ITurretInfo;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.api.turret.ITurretRAM;
import de.sanandrew.mods.turretmod.api.turret.TurretAttributes;
import de.sanandrew.mods.turretmod.client.audio.SoundLaser;
import de.sanandrew.mods.turretmod.util.Resources;
import de.sanandrew.mods.turretmod.util.TurretModRebirth;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class TurretLaser
        implements ITurret
{
    public static final ResourceLocation ITEM_MODEL = new ResourceLocation(TmrConstants.ID, "turrets/turret_laser");
    public static final UUID TIII_UUID = UUID.fromString("F6196022-3F9D-4D3F-B3C1-9ED644DB436B");

    private static final AxisAlignedBB RANGE_BB = new AxisAlignedBB(-24.0D, -4.0D, -24.0D, 24.0D, 12.0D, 24.0D);

    @Override
    public void applyEntityAttributes(ITurretInst turretInst) {
        turretInst.getEntity().getEntityAttribute(TurretAttributes.MAX_RELOAD_TICKS).setBaseValue(5.0D);
        turretInst.getEntity().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
    }

    @Override
    public ResourceLocation getStandardTexture(ITurretInst turretInst) {
        return Resources.TURRET_T3_LASER.getResource();
    }

    @Override
    public ResourceLocation getGlowTexture(ITurretInst turretInst) {
        return Resources.TURRET_T3_LASER_GLOW.getResource();
    }

    @Override
    public void onUpdate(ITurretInst turretInst) {
        if( turretInst.getEntity().world.isRemote ) {
            TurretModRebirth.proxy.playTurretLaser(turretInst);
        }
    }

    @Override
    public AxisAlignedBB getRangeBB(ITurretInst turretInst) {
        return RANGE_BB;
    }

    @Override
    public SoundEvent getShootSound(ITurretInst turretInst) {
        return null;
    }

    @Override
    public String getName() {
        return "iii_laser";
    }

    @Override
    public UUID getId() {
        return TurretLaser.TIII_UUID;
    }

    @Override
    public ResourceLocation getItemModel() {
        return TurretLaser.ITEM_MODEL;
    }

    @Override
    public int getTier() {
        return 3;
    }

    @Override
    public ITurretInfo getInfo() {
        return MyInfo.INSTANCE;
    }

    public static class MyRAM
            implements ITurretRAM
    {
        @SideOnly(Side.CLIENT)
        public SoundLaser laserSound;
    }

    public static final class MyInfo implements ITurretInfo
    {
        static final ITurretInfo INSTANCE = new MyInfo();

        @Override
        public float getHealth() {
            return 40.0F;
        }

        @Override
        public int getAmmoCapacity() {
            return 256;
        }

        @Override
        public String getRange() {
            return "24";
        }
    }
}