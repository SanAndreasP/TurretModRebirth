/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.api.turret;

import de.sanandrew.mods.turretmod.api.TmrConstants;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

public final class TurretAttributes
{
    public static final IAttribute MAX_AMMO_CAPACITY    = new RangedAttribute(null, TmrConstants.ID + ".maxAmmoCapacity", 256.0D, 0.0D, Short.MAX_VALUE).setShouldWatch(true);
    public static final IAttribute MAX_RELOAD_TICKS     = new RangedAttribute(null, TmrConstants.ID + ".maxReloadTicks", 20.0D, 0.0D, Double.MAX_VALUE).setShouldWatch(true);
    public static final IAttribute MAX_INIT_SHOOT_TICKS = new RangedAttribute(null, TmrConstants.ID + ".maxInitShootTicks", 20.0D, 0.0D, Short.MAX_VALUE).setShouldWatch(true);
}
