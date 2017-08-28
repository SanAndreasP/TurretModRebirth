/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.registry.turret;

import de.sanandrew.mods.turretmod.api.turret.EntityTurret;
import de.sanandrew.mods.turretmod.api.turret.ITurretInfo;
import de.sanandrew.mods.turretmod.entity.turret.EntityTurretCrossbow;
import de.sanandrew.mods.turretmod.registry.assembly.TurretAssemblyRecipes;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class TurretInfoCrossbow
        implements ITurretInfo
{
    @Override
    public String getName() {
        return "turret_i_crossbow";
    }

    @Override
    public UUID getUUID() {
        return EntityTurretCrossbow.TI_UUID;
    }

    @Override
    public Class<? extends EntityTurret> getTurretClass() {
        return EntityTurretCrossbow.class;
    }

    @Override
    public float getTurretHealth() {
        return 20.0F;
    }

    @Override
    public int getBaseAmmoCapacity() {
        return 256;
    }

    @Override
    public ResourceLocation getModel() {
        return EntityTurretCrossbow.ITEM_MODEL;
    }

    @Override
    public UUID getRecipeId() {
        return TurretAssemblyRecipes.TURRET_MK1_CB;
    }

    @Override
    public String getInfoRange() {
        return "16";
    }
}
