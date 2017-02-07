/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.registry.upgrades;

import de.sanandrew.mods.sanlib.lib.util.EntityUtils;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.turret.EntityTurret;
import de.sanandrew.mods.turretmod.registry.assembly.TurretAssemblyRecipes;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public abstract class UpgradeHealth
        implements TurretUpgrade
{
    private final ResourceLocation itemModel;
    private final String name;
    private AttributeModifier modifier;

    public UpgradeHealth(String name, String modUUID) {
        this.name = name;
        this.modifier = new AttributeModifier(UUID.fromString(modUUID), String.format("%s:%s", TmrConstants.ID, name), 0.25D, EntityUtils.ATTR_ADD_PERC_VAL_TO_SUM);
        this.itemModel = new ResourceLocation(TmrConstants.ID, "upgrades/" + name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getModId() {
        return TmrConstants.ID;
    }

    @Override
    public ResourceLocation getModel() {
        return this.itemModel;
    }

    @Override
    public boolean isTurretApplicable(Class<? extends EntityTurret> turretCls) {
        return true;
    }

    @Override
    public void onApply(EntityTurret turret) {
        if( !turret.world.isRemote ) {
            IAttributeInstance attrib = turret.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
            if( attrib.getModifier(this.modifier.getID()) != null ) {
                attrib.removeModifier(this.modifier);
            }

            attrib.applyModifier(this.modifier);
        }
    }

    @Override
    public void onRemove(EntityTurret turret) {
        if( !turret.world.isRemote ) {
            IAttributeInstance attrib = turret.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
            if( attrib.getModifier(this.modifier.getID()) != null ) {
                attrib.removeModifier(this.modifier);
                turret.setHealth(Math.min(turret.getHealth(), turret.getMaxHealth()));
            }
        }
    }

    public static class UpgradeHealthMK1
            extends UpgradeHealth
    {

        public UpgradeHealthMK1() {
            super("health_i", "673176FC-51F9-4CBC-BA12-5073B6867644");
        }

        @Override
        public TurretUpgrade getDependantOn() {
            return null;
        }

        @Override
        public UUID getRecipeId() {
            return TurretAssemblyRecipes.UPG_HEALTH_MK1;
        }
    }

    public static class UpgradeHealthMK2
            extends UpgradeHealth
    {
        private final TurretUpgrade dependant;

        public UpgradeHealthMK2() {
            super("health_ii", "B7E5ADFA-517C-4167-A2FD-E0D31FA6E9BE");
            this.dependant = UpgradeRegistry.INSTANCE.getUpgrade(UpgradeRegistry.HEALTH_I);
        }

        @Override
        public TurretUpgrade getDependantOn() {
            return this.dependant;
        }

        @Override
        public UUID getRecipeId() {
            return TurretAssemblyRecipes.UPG_HEALTH_MK2;
        }
    }

    public static class UpgradeHealthMK3
            extends UpgradeHealth
    {
        private final TurretUpgrade dependant;

        public UpgradeHealthMK3() {
            super("health_iii", "D49F43AE-5EA4-4DD2-B08A-9B5F1966C091");
            this.dependant = UpgradeRegistry.INSTANCE.getUpgrade(UpgradeRegistry.HEALTH_II);
        }

        @Override
        public TurretUpgrade getDependantOn() {
            return this.dependant;
        }

        @Override
        public UUID getRecipeId() {
            return TurretAssemblyRecipes.UPG_HEALTH_MK3;
        }
    }

    public static class UpgradeHealthMK4
            extends UpgradeHealth
    {
        private final TurretUpgrade dependant;

        public UpgradeHealthMK4() {
            super("health_iv", "9431A60C-B995-4547-B143-2BEDC67467E1");
            this.dependant = UpgradeRegistry.INSTANCE.getUpgrade(UpgradeRegistry.HEALTH_III);
        }

        @Override
        public TurretUpgrade getDependantOn() {
            return this.dependant;
        }

        @Override
        public UUID getRecipeId() {
            return TurretAssemblyRecipes.UPG_HEALTH_MK4;
        }
    }
}