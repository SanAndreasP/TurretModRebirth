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
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.api.turret.TurretAttributes;
import de.sanandrew.mods.turretmod.util.Resources;
import de.sanandrew.mods.turretmod.util.TmrConfiguration;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.UUID;

public class TurretCrossbow
        implements ITurret
{
    public static final ResourceLocation ITEM_MODEL = new ResourceLocation(TmrConstants.ID, "turrets/turret_crossbow");
    public static final UUID TI_UUID = UUID.fromString("50E1E69C-395C-486C-BB9D-41E82C8B22E2");

    private static final AxisAlignedBB RANGE_BB = new AxisAlignedBB(-16.0D, -4.0D, -16.0D, 16.0D, 8.0D, 16.0D);

    @Override
    public void applyEntityAttributes(ITurretInst turretInst) {
        turretInst.getEntity().getEntityAttribute(TurretAttributes.MAX_RELOAD_TICKS).setBaseValue(20.0D);
        turretInst.getEntity().getEntityAttribute(TurretAttributes.MAX_AMMO_CAPACITY).setBaseValue(TmrConfiguration.ConfTurret.crossbowMaxAmmoCapacity);
        turretInst.getEntity().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(TmrConfiguration.ConfTurret.crossbowMaxHealth);
    }

    @Override
    public ResourceLocation getStandardTexture(ITurretInst turretInst) {
        return Resources.TURRET_T1_CROSSBOW.resource;
    }

    @Override
    public ResourceLocation getGlowTexture(ITurretInst turretInst) {
        return Resources.TURRET_T1_CROSSBOW_GLOW.resource;
    }

    @Override
    public AxisAlignedBB getRangeBB(ITurretInst turretInst) {
        return RANGE_BB;
    }

    @Override
    public SoundEvent getShootSound(ITurretInst turretInst) {
        return SoundEvents.BLOCK_DISPENSER_LAUNCH;
    }

    @Override
    public String getName() {
        return "i_crossbow";
    }

    @Override
    public UUID getId() {
        return TurretCrossbow.TI_UUID;
    }

    @Override
    public ResourceLocation getItemModel() {
        return TurretCrossbow.ITEM_MODEL;
    }

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public float getHealth() {
        return TmrConfiguration.ConfTurret.crossbowMaxHealth;
    }

    @Override
    public int getAmmoCapacity() {
        return TmrConfiguration.ConfTurret.crossbowMaxAmmoCapacity;
    }
}
