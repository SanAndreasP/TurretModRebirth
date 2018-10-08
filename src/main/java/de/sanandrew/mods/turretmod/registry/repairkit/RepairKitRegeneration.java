/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.registry.repairkit;

import de.sanandrew.mods.turretmod.api.repairkit.TurretRepairKit;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class RepairKitRegeneration
        implements TurretRepairKit
{
    private final String name;
    private final UUID uuid;
    private final float heal;
    private final ResourceLocation itemModel;
    private final int regenLvl;
    private final int regenTime;

    RepairKitRegeneration(String name, UUID uuid, float heal, ResourceLocation model, int level, int time) {
        this.name = name;
        this.uuid = uuid;
        this.heal = heal;
        this.itemModel = model;
        this.regenLvl = level;
        this.regenTime = time;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final UUID getUUID() {
        return this.uuid;
    }

    @Override
    public final float getHealAmount() {
        return this.heal;
    }

    @Override
    public final void onHeal(ITurretInst turret) {
        turret.get().addPotionEffect(new PotionEffect(MobEffects.REGENERATION, this.regenTime, this.regenLvl, true, false));
    }

    @Override
    public boolean isApplicable(ITurretInst turret) {
        return turret.get().getHealth() <= turret.get().getMaxHealth() - this.heal && turret.get().getActivePotionEffect(MobEffects.REGENERATION) == null;
    }

    @Override
    public ResourceLocation getModel() {
        return this.itemModel;
    }
}
