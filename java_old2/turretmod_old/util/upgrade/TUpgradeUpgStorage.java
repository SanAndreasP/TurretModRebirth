/**
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.util.upgrade;

import de.sanandrew.core.manpack.util.EnumAttrModifierOperation;
import de.sanandrew.mods.turretmod.api.Turret;
import de.sanandrew.mods.turretmod.api.TurretUpgrade;
import de.sanandrew.mods.turretmod.entity.turret.TurretAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public abstract class TUpgradeUpgStorage
        extends TurretUpgradeBase
{
    protected AttributeModifier modifier;

    private TUpgradeUpgStorage(String upgName, String texture, TurretUpgrade dependUpgrade) {
        super(upgName, texture, dependUpgrade);
    }

    @Override
    public void onApply(Turret turret) {
        if( !turret.getEntity().worldObj.isRemote ) {
            turret.getEntity().getEntityAttribute(TurretAttributes.MAX_UPGRADE_SLOTS).applyModifier(modifier);
        }
    }

    @Override
    public void onRemove(Turret turret) {
        turret.getEntity().getEntityAttribute(TurretAttributes.MAX_UPGRADE_SLOTS).removeModifier(modifier);
    }

    public static class TUpgradeUpgStorageI
            extends TUpgradeUpgStorage
    {
        public TUpgradeUpgStorageI() {
            super("upgStorageI", "upgrades/upg_storage_i", null);
            this.modifier = new AttributeModifier(UUID.fromString("9EB0538E-AF02-4700-AAE4-E3DB7C78DFC0"), "upgStorage_1", 9.0D, EnumAttrModifierOperation.ADD_VAL_TO_BASE.ordinal());
        }
    }

    public static class TUpgradeUpgStorageII
            extends TUpgradeUpgStorage
    {
        public TUpgradeUpgStorageII(TurretUpgrade depInst) {
            super("upgStorageII", "upgrades/upg_storage_ii", depInst);
            this.modifier = new AttributeModifier(UUID.fromString("DEBD1322-ACA8-48A9-B4D8-368AF4AEDADC"), "upgStorage_2", 9.0D, EnumAttrModifierOperation.ADD_VAL_TO_BASE.ordinal());
        }
    }

    public static class TUpgradeUpgStorageIII
            extends TUpgradeUpgStorage
    {
        public TUpgradeUpgStorageIII(TurretUpgrade depInst) {
            super("upgStorageIII", "upgrades/upg_storage_iii", depInst);
            this.modifier = new AttributeModifier(UUID.fromString("B9044E92-60D7-453E-A1E9-630761D2B35D"), "upgStorage_3", 9.0D, EnumAttrModifierOperation.ADD_VAL_TO_BASE.ordinal());
        }
    }
}
