/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.registry.turret.forcefield;

import de.sanandrew.mods.sanlib.lib.ColorObj;
import de.sanandrew.mods.turretmod.api.turret.IForcefieldProvider;
import net.minecraft.util.math.AxisAlignedBB;

public final class ForcefieldRecovery
        implements IForcefieldProvider
{
    private final Forcefield delegate;

    ForcefieldRecovery(Forcefield delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isShieldActive() {
        return this.delegate.isInRecovery() && this.delegate.turretInst.isActive();
    }

    @Override
    public AxisAlignedBB getShieldBoundingBox() {
        return this.delegate.getShieldBoundingBox();
    }

    @Override
    public int getShieldColor() {
        float perc = this.delegate.recovery;
        if( perc < 0.9F ) {
            perc = perc / 0.9F;

            ColorObj newClr = new ColorObj(Forcefield.CRIT_COLOR);
            newClr.setAlpha(Math.round(Forcefield.CRIT_COLOR.fAlpha() * 255.0F * perc));

            return newClr.getColorInt();
        } else {
            return this.delegate.getCritColor((perc - 0.9F) * 10.0F);
        }
    }

    @Override
    public boolean cullShieldFaces() {
        return false;
    }

    @Override
    public boolean hasSmoothFadeOut() {
        return false;
    }

    @Override
    public boolean renderFull() {
        return true;
    }
}
