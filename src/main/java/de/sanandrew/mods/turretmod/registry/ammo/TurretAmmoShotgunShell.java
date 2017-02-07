/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.registry.ammo;

import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.entity.projectile.EntityProjectilePebble;
import de.sanandrew.mods.turretmod.api.turret.EntityTurret;
import de.sanandrew.mods.turretmod.entity.turret.EntityTurretShotgun;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import de.sanandrew.mods.turretmod.registry.assembly.TurretAssemblyRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public abstract class TurretAmmoShotgunShell
        implements TurretAmmo<EntityProjectilePebble>
{
    public static final UUID SHELL_UUID = UUID.fromString("3B3AA3F7-DA37-4B92-8F18-53694361447F");
    public static final UUID PACK_UUID = UUID.fromString("6F3DB2C0-E881-462A-AC3A-6358EA7A1FE8");
    private static final UUID TYPE_UUID = SHELL_UUID;

    private final String name;
    private final UUID uuid;
    private final int capacity;
    private final ResourceLocation itemModel;

    public TurretAmmoShotgunShell(boolean isMulti, String modelName) {
        this.name = isMulti ? "sgshell_lrg" : "sgshell_sng";
        this.uuid = isMulti ? PACK_UUID : SHELL_UUID;
        this.capacity = isMulti ? 16 : 1;
        this.itemModel = new ResourceLocation(TmrConstants.ID, "turret_ammo/" + modelName);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getId() {
        return this.uuid;
    }

    @Override
    public int getAmmoCapacity() {
        return this.capacity;
    }

    @Override
    public Class<EntityProjectilePebble> getEntityClass() {
        return EntityProjectilePebble.class;
    }

    @Override
    public Class<? extends EntityTurret> getTurret() {
        return EntityTurretShotgun.class;
    }

    @Override
    public float getInfoDamage() {
        return 0.5F;
    }

    @Override
    public UUID getTypeId() {
        return TYPE_UUID;
    }

    @Override
    public UUID getGroupId() {
        return TYPE_UUID;
    }

    @Override
    public String getInfoName() {
        return "sgshell";
    }

    @Override
    public ItemStack getStoringAmmoItem() {
        return ItemRegistry.turret_ammo.getAmmoItem(1, AmmoRegistry.INSTANCE.getType(SHELL_UUID));
    }

    @Override
    public EntityProjectilePebble getEntity(EntityTurret turret) {
        return new EntityProjectilePebble(turret.world, turret, turret.getTargetProcessor().getTarget());
    }

    @Override
    public ResourceLocation getModel() {
        return this.itemModel;
    }

    public static class Single
            extends TurretAmmoShotgunShell
    {
        public Single() {
            super(false, "shotgun_shell");
        }

        @Override
        public UUID getRecipeId() {
            return TurretAssemblyRecipes.SGSHELL_SNG;
        }
    }

    public static class Multi
            extends TurretAmmoShotgunShell
    {
        public Multi() {
            super(true, "shotgun_shell_pack");
        }

        @Override
        public UUID getRecipeId() {
            return TurretAssemblyRecipes.SGSHELL_MTP;
        }
    }
}