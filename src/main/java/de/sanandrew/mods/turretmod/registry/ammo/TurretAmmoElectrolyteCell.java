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
import de.sanandrew.mods.turretmod.api.ammo.IAmmunition;
import de.sanandrew.mods.turretmod.api.turret.ITurret;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.registry.turret.Turrets;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TurretAmmoElectrolyteCell
        implements IAmmunition
{
    private final String name;
    private final UUID id;
    private final int capacity;
    private final ResourceLocation itemModel;

    public TurretAmmoElectrolyteCell(boolean isMulti) {
        this.name = isMulti ? "eleccell_pack" : "eleccell";
        this.id = isMulti ? Ammunitions.ELECTROLYTECELL_PACK : Ammunitions.ELECTROLYTECELL;
        this.capacity = isMulti ? 32 : 2;
        this.itemModel = new ResourceLocation(TmrConstants.ID, "turret_ammo/" + this.name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public int getAmmoCapacity() {
        return this.capacity;
    }

    @Override
    public Class getEntityClass() {
        return null;
    }

    @Override
    public ITurret getTurret() {
        return Turrets.SHIELDGEN;
    }

    @Override
    public float getInfoDamage() {
        return 0.0F;
    }

    @Override
    public UUID getTypeId() {
        return Ammunitions.ELECTROLYTECELL;
    }

    @Override
    public UUID getGroupId() {
        return Ammunitions.ELECTROLYTECELL;
    }

    @Override
    public String getInfoName() {
        return "eleccell";
    }

    @Override
    @Nonnull
    public ItemStack getStoringAmmoItem() {
        return AmmunitionRegistry.INSTANCE.getAmmoItem(AmmunitionRegistry.INSTANCE.getType(Ammunitions.ELECTROLYTECELL));
    }

    @Override
    public Entity getEntity(ITurretInst turretInst) {
        return null;
    }

    @Override
    public ResourceLocation getModel() {
        return this.itemModel;
    }
}