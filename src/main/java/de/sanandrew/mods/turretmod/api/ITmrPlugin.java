package de.sanandrew.mods.turretmod.api;

import de.sanandrew.mods.turretmod.api.ammo.IAmmunitionRegistry;
import de.sanandrew.mods.turretmod.api.ammo.IProjectileRegistry;
import de.sanandrew.mods.turretmod.api.assembly.IAssemblyManager;
import de.sanandrew.mods.turretmod.api.client.render.IRender;
import de.sanandrew.mods.turretmod.api.client.render.IRenderRegistry;
import de.sanandrew.mods.turretmod.api.client.tcu.ILabelRegistry;
import de.sanandrew.mods.turretmod.api.client.turret.ITurretRenderRegistry;
import de.sanandrew.mods.turretmod.api.electrolytegen.IElectrolyteManager;
import de.sanandrew.mods.turretmod.api.repairkit.IRepairKitRegistry;
import de.sanandrew.mods.turretmod.api.turret.IGuiTcuRegistry;
import de.sanandrew.mods.turretmod.api.turret.ITurretRegistry;
import de.sanandrew.mods.turretmod.api.upgrade.IUpgradeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ITmrPlugin
{
    default void preInit(ITmrUtils utils) { }

    default void registerAssemblyRecipes(IAssemblyManager registry) { }

    default void registerElectrolyteRecipes(IElectrolyteManager registry) { }

    default void registerTurrets(ITurretRegistry registry) { }

    default void registerRepairKits(IRepairKitRegistry registry) { }

    default void registerAmmo(IAmmunitionRegistry registry) { }

    default void registerUpgrades(IUpgradeRegistry registry) { }

    default void postInit() { }

    default void registerTcuEntries(IGuiTcuRegistry registry) { }

    default void registerProjectiles(IProjectileRegistry registry) { }

    @SideOnly(Side.CLIENT)
    default void registerTurretRenderer(ITurretRenderRegistry<?> registry) { }

    @SideOnly(Side.CLIENT)
    default void registerTurretRenderLayers(ITurretRenderRegistry<?> registry) { }

    @SideOnly(Side.CLIENT)
    default <T extends Entity> void registerProjectileRenderer(IRenderRegistry<ResourceLocation, T, IRender<T>> registry) { }

    @SideOnly(Side.CLIENT)
    default void registerTcuLabelElements(ILabelRegistry registry) { }

    @SideOnly(Side.CLIENT)
    default void registerTcuGuis(IGuiTcuRegistry registry) { }
}
