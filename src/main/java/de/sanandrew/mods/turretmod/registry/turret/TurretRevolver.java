/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.registry.turret;

import de.sanandrew.mods.sanlib.lib.Tuple;
import de.sanandrew.mods.sanlib.lib.util.config.Category;
import de.sanandrew.mods.sanlib.lib.util.config.Range;
import de.sanandrew.mods.sanlib.lib.util.config.Value;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.turret.ITurret;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.api.turret.ITurretRAM;
import de.sanandrew.mods.turretmod.util.EnumParticle;
import de.sanandrew.mods.turretmod.util.Resources;
import de.sanandrew.mods.turretmod.util.Sounds;
import de.sanandrew.mods.turretmod.util.TurretModRebirth;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.UUID;

@Category("revolver")
@SuppressWarnings("WeakerAccess")
public class TurretRevolver
        implements ITurret
{
    public static final ResourceLocation ITEM_MODEL = new ResourceLocation(TmrConstants.ID, "turrets/turret_revolver");
    private static final UUID ID = UUID.fromString("4449D836-F122-409A-8E6C-D7B7438FD08C");

    private static AxisAlignedBB rangeBB;

    @Value(comment = "Maximum health this turret has.", range = @Range(minD = 0.1D, maxD = 1024.0D), reqWorldRestart = true)
    public static float health = 30.0F;
    @Value(comment = "Capacity of ammo rounds this turret can hold.", range = @Range(minI = 1, maxI = Short.MAX_VALUE), reqWorldRestart = true)
    public static int ammoCapacity = 256;
    @Value(comment = "Maximum tick time between shots. 20 ticks = 1 second.", range = @Range(minI = 1), reqWorldRestart = true)
    public static int reloadTicks = 15;
    @Value(comment = "Horizontal length of half the edge of the targeting box. The total edge length is [value * 2], with the turret centered in it.", range = @Range(minD = 1.0D), reqMcRestart = true)
    public static double rangeH = 20.0D;
    @Value(comment = "Vertical length of the edge of the targeting box, from the turret upwards.", range = @Range(minD = 1.0D), reqMcRestart = true)
    public static double rangeU = 10.0D;
    @Value(comment = "Vertical length of the edge of the targeting box, from the turret downwards.", range = @Range(minD = 1.0D), reqMcRestart = true)
    public static double rangeD = 4.0D;

    @Override
    public void onUpdate(ITurretInst turretInst) {
        EntityLiving turretL = turretInst.get();

        MyRAM ram = turretInst.getRAM(MyRAM::new);
        ram.prevBarrelLeft = ram.barrelLeft;
        ram.prevBarrelRight = ram.barrelRight;

        if( ram.barrelLeft < 1.0F ) {
            ram.barrelLeft += 0.06F * 20.0F / turretInst.getTargetProcessor().getMaxShootTicks();
        } else {
            ram.barrelLeft = 1.0F;
        }

        if( ram.barrelRight < 1.0F ) {
            ram.barrelRight += 0.06F * 20.0F / turretInst.getTargetProcessor().getMaxShootTicks();
        } else {
            ram.barrelRight = 1.0F;
        }

        if( turretInst.wasShooting() ) {
            float partShift;
            if( ram.isLeftShot ) {
                ram.barrelRight = 0.0F;
                ram.isLeftShot = false;
                partShift = 10.0F;
            } else {
                ram.barrelLeft = 0.0F;
                ram.isLeftShot = true;
                partShift = -10.0F;
            }

            if( turretL.world.isRemote ) {
                TurretModRebirth.proxy.spawnParticle(EnumParticle.SHOTGUN_SHOT, turretL.posX, turretL.posY + 1.5F, turretL.posZ,
                                                     new Tuple(turretL.rotationYawHead + partShift, turretL.rotationPitch));
            }
        }
    }

    @Override
    public ResourceLocation getStandardTexture(ITurretInst turretInst) {
        return Resources.TURRET_T2_REVOLVER.resource;
    }

    @Override
    public ResourceLocation getGlowTexture(ITurretInst turretInst) {
        return Resources.TURRET_T2_REVOLVER_GLOW.resource;
    }

    @Override
    public AxisAlignedBB getRangeBB(ITurretInst turretInst) {
        if( rangeBB == null ) {
            rangeBB = new AxisAlignedBB(-rangeH, -rangeD, -rangeH, rangeH, rangeU, rangeH);
        }
        return rangeBB;
    }

    @Override
    public SoundEvent getShootSound(ITurretInst turretInst) {
        return Sounds.SHOOT_REVOLVER;
    }

    @Override
    public String getName() {
        return "ii_revolver";
    }

    @Override
    public UUID getId() {
        return TurretRevolver.ID;
    }

    @Override
    public ResourceLocation getItemModel() {
        return TurretRevolver.ITEM_MODEL;
    }

    @Override
    public int getTier() {
        return 2;
    }

    public static class MyRAM implements ITurretRAM
    {
        public float barrelLeft = 1.0F;
        public float barrelRight = 1.0F;

        public float prevBarrelLeft = 1.0F;
        public float prevBarrelRight = 1.0F;

        boolean isLeftShot = false;
    }

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public int getAmmoCapacity() {
        return ammoCapacity;
    }

    @Override
    public int getReloadTicks() {
        return reloadTicks;
    }
}
