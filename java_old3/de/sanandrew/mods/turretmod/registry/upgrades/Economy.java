/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.registry.upgrades;

import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.upgrade.IUpgrade;
import net.minecraft.util.ResourceLocation;

public abstract class Economy
        implements IUpgrade
{
    private final ResourceLocation id;

    Economy(String name) {
        this.id = new ResourceLocation(TmrConstants.ID, "upgrade_" + name);
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    static class MK1
            extends Economy
    {
        MK1() {
            super("economy_1");
        }
    }

    static class MK2
            extends Economy
    {
        MK2() {
            super("economy_2");
        }

        @Override
        public IUpgrade getDependantOn() {
            return Upgrades.ECONOMY_I;
        }
    }

    static class MKInf
            extends Economy
    {
        MKInf() {
            super("economy_infinite");
        }

        @Override
        public IUpgrade getDependantOn() {
            return Upgrades.ECONOMY_II;
        }
    }
}
