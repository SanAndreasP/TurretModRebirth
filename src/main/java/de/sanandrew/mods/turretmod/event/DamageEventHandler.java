/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.event;

import de.sanandrew.mods.turretmod.api.turret.IUpgradeProcessor;
import de.sanandrew.mods.turretmod.entity.turret.EntityTurret;
import de.sanandrew.mods.turretmod.registry.upgrades.Upgrades;
import de.sanandrew.mods.turretmod.registry.upgrades.shield.ShieldPersonal;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DamageEventHandler
{
    @SubscribeEvent
    public void onDamage(LivingHurtEvent event) {
        if( event.getEntity() instanceof EntityTurret) {
            IUpgradeProcessor proc = ((EntityTurret) event.getEntity()).getUpgradeProcessor();
            if( proc.hasUpgrade(Upgrades.SHIELD_PERSONAL) ) {
                ShieldPersonal upgInst = proc.getUpgradeInstance(Upgrades.SHIELD_PERSONAL);
                float restDmg = upgInst.damage(event.getAmount());
                if( restDmg <= 0.0F ) {
                    event.setCanceled(true);
                } else {
                    event.setAmount(restDmg);
                }
                proc.syncUpgrade(Upgrades.SHIELD_PERSONAL);
            }
        }
    }
}