/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.registry.projectile;

import de.sanandrew.mods.sanlib.lib.util.config.Category;
import de.sanandrew.mods.sanlib.lib.util.config.Range;
import de.sanandrew.mods.sanlib.lib.util.config.Value;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.ammo.IProjectile;
import de.sanandrew.mods.turretmod.api.ammo.IProjectileInst;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.entity.turret.EntityTurretProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import org.apache.commons.lang3.mutable.MutableFloat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Category("laser")
@SuppressWarnings("WeakerAccess")
public class Laser
        implements IProjectile
{
    static final ResourceLocation ID1 = new ResourceLocation(TmrConstants.ID, "laser.normal");
    static final ResourceLocation ID2 = new ResourceLocation(TmrConstants.ID, "laser.pure");

    @Value(comment = "Base damage this projectile can deal to a target.", range = @Range(minD = 0.0D, maxD = 1024.0D))
    public static float damage = 2.0F;
    @Value(comment = "Multiplier applied to the speed with which this projectile travels.", range = @Range(minD = 0.0D, maxD = 256.0D))
    public static float speed = 20.0F;
    @Value(comment = "How much this projectile curves down/up. negative values let it go up, whereas positive values go down.", range = @Range(minD = -10.0D, maxD = 10.0D))
    public static float arc = 0.0F;
    @Value(comment = "Horizontal knockback strength this projectile can apply. Vanilla arrows have a value of 0.1.", range = @Range(minD = 0.0D, maxD = 256.0D))
    public static float knockbackH = 0.0F;
    @Value(comment = "Vertical (y) knockback strength this projectile can apply. Vanilla arrows have a value of 0.1.", range = @Range(minD = 0.0D, maxD = 256.0D))
    public static float knockbackV = 0.0F;
    @Value(comment = "How much more inaccurate this projectile's trajectory vector becomes. Higher values result in less accuracy.", range = @Range(minD = 0.0D, maxD = 10.0D))
    public static double scatter = 0.0D;
    @Value(comment = "How long an entity hit with this projectile is set on fire in seconds. 0 disables this.", range = @Range(minI = 0))
    public static int fireTime = 2;

    private final boolean isBlue;
    private final ResourceLocation id;

    Laser(ResourceLocation id) {
        this.id = id;
        this.isBlue = id.equals(ID2);
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public float getArc() {
        return arc;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getDamage() {
        return damage;
    }

    @Override
    public float getKnockbackHorizontal() {
        return knockbackH;
    }

    @Override
    public float getKnockbackVertical() {
        return knockbackV;
    }

    @Override
    public SoundEvent getRicochetSound() {
        return SoundEvents.BLOCK_FIRE_EXTINGUISH;
    }

    @Override
    public double getScatterValue() {
        return scatter;
    }

    @Override
    public DamageSource getCustomDamageSrc(@Nullable ITurretInst turret, @Nonnull IProjectileInst projectile, Entity target, TargetType type) {
        DamageSource ds = EntityTurretProjectile.getDamageSource(turret, projectile, type);

        if( !this.isBlue ) {
            ds.setFireDamage();
        }

        return ds;
    }

    @Override
    public boolean onDamageEntityPre(@Nullable ITurretInst turret, @Nonnull IProjectileInst projectile, Entity target, DamageSource damageSrc, MutableFloat damage) {
        boolean flammable = !target.isImmuneToFire();

        if( !this.isBlue ) {
            return flammable;
        } else if( flammable ) {
            damage.setValue(damage.floatValue() * 1.25F);
        }

        return true;
    }

    @Override
    public void onDamageEntityPost(@Nullable ITurretInst turret, @Nonnull IProjectileInst projectile, Entity target, DamageSource damageSrc) {
        if( fireTime > 0 ) {
            target.setFire(fireTime);
        }
    }
}
