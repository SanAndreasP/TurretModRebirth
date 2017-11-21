/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.entity.projectile;

import de.sanandrew.mods.turretmod.util.TmrConfiguration;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityProjectileCrossbowBolt
        extends EntityTurretProjectile
{
    public EntityProjectileCrossbowBolt(World world) {
        super(world);
    }

    public EntityProjectileCrossbowBolt(World world, Entity shooter, Entity target) {
        super(world, shooter, target);
    }

    public EntityProjectileCrossbowBolt(World world, Entity shooter, Vec3d shootingVec) {
        super(world, shooter, shootingVec);
    }

    @Override
    public float getArc() {
        return 0.4F;
    }

    @Override
    public SoundEvent getRicochetSound() {
        return SoundEvents.ENTITY_ARROW_HIT;
    }

    @Override
    public float getInitialSpeedMultiplier() {
        return 1.0F;
    }

    @Override
    public float getDamage() {
        return TmrConfiguration.ConfTurret.crossbowProjDamage;
    }

    @Override
    public float getKnockbackStrengthH() {
        return TmrConfiguration.ConfTurret.crossbowProjKnockbackH;
    }

    @Override
    public float getKnockbackStrengthV() {
        return TmrConfiguration.ConfTurret.crossbowProjKnockbackV;
    }
}
