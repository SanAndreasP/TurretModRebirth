package de.sanandrew.mods.turretmod.api;

import de.sanandrew.mods.turretmod.api.ammo.IAmmunitionRegistry;
import de.sanandrew.mods.turretmod.api.ammo.IProjectileRegistry;
import de.sanandrew.mods.turretmod.api.client.tcu.ILabelRegistry;
import de.sanandrew.mods.turretmod.api.repairkit.IRepairKitRegistry;
import de.sanandrew.mods.turretmod.api.turret.IGuiTcuRegistry;
import de.sanandrew.mods.turretmod.api.turret.ITurretRegistry;
import de.sanandrew.mods.turretmod.api.upgrade.IUpgradeRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ITmrPlugin
{
    default void preSetup(ITmrUtils utils) { }

    default void registerTurrets(ITurretRegistry registry) { }

    default void registerRepairKits(IRepairKitRegistry registry) { }

    default void registerAmmo(IAmmunitionRegistry registry) { }

    default void registerUpgrades(IUpgradeRegistry registry) { }

    default void setup() { }

    default void registerTcuEntries(IGuiTcuRegistry registry) { }

    default void registerProjectiles(IProjectileRegistry registry) { }

//    @OnlyIn(Dist.CLIENT)
//    default <T extends Entity> void registerProjectileRenderer(IRenderRegistry<ResourceLocation, T, IRender<T>> registry) { }

    @OnlyIn(Dist.CLIENT)
    default void registerTcuLabelElements(ILabelRegistry registry) { }

    @OnlyIn(Dist.CLIENT)
    default void registerTcuGuis(IGuiTcuRegistry registry) { }
}
