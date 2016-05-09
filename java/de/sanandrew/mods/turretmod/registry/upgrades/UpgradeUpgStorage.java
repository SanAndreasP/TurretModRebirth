/**
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.registry.upgrades;

import de.sanandrew.mods.turretmod.entity.turret.EntityTurret;
import de.sanandrew.mods.turretmod.util.TurretModRebirth;
import net.minecraft.nbt.NBTTagCompound;

public abstract class UpgradeUpgStorage
        implements TurretUpgrade
{
    private final String name;

    public UpgradeUpgStorage(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getModId() {
        return TurretModRebirth.ID;
    }

    @Override
    public String getIconTexture() {
        return TurretModRebirth.ID + ":upgrades/" + this.name;
    }

    @Override
    public boolean isTurretApplicable(Class<? extends EntityTurret> turretCls) {
        return true;
    }

    @Override
    public void onApply(EntityTurret turret) {}

    @Override
    public void onRemove(EntityTurret turret) {}

    @Override
    public void onLoad(EntityTurret turret, NBTTagCompound nbt) {}

    @Override
    public void onSave(EntityTurret turret, NBTTagCompound nbt) {}

    public static class UpgradeStorageMK1
            extends UpgradeUpgStorage
    {

        public UpgradeStorageMK1() {
            super("upg_storage_i");
        }

        @Override
        public TurretUpgrade getDependantOn() {
            return null;
        }
    }

    public static class UpgradeStorageMK2
            extends UpgradeUpgStorage
    {
        private final TurretUpgrade dependant;

        public UpgradeStorageMK2() {
            super("upg_storage_ii");
            this.dependant = UpgradeRegistry.INSTANCE.getUpgrade(UpgradeRegistry.UPG_STORAGE_I);
        }

        @Override
        public TurretUpgrade getDependantOn() {
            return this.dependant;
        }
    }

    public static class UpgradeStorageMK3
            extends UpgradeUpgStorage
    {
        private final TurretUpgrade dependant;

        public UpgradeStorageMK3() {
            super("upg_storage_iii");
            this.dependant = UpgradeRegistry.INSTANCE.getUpgrade(UpgradeRegistry.UPG_STORAGE_II);
        }

        @Override
        public TurretUpgrade getDependantOn() {
            return this.dependant;
        }
    }
}
