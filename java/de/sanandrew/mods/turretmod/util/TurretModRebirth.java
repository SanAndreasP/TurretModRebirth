/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.util;

import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.entity.turret.TargetProcessor;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.registry.ammo.AmmoRegistry;
import de.sanandrew.mods.turretmod.registry.assembly.TurretAssemblyRecipes;
import de.sanandrew.mods.turretmod.registry.medpack.RepairKitRegistry;
import de.sanandrew.mods.turretmod.registry.turret.TurretRegistry;
import de.sanandrew.mods.turretmod.registry.upgrades.UpgradeRegistry;
import de.sanandrew.mods.turretmod.tileentity.TileEntityElectrolyteGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = TurretModRebirth.ID, version = TurretModRebirth.VERSION, name = TurretModRebirth.NAME, guiFactory = TurretModRebirth.GUI_FACTORY, dependencies = TurretModRebirth.DEPENDENCIES)
public class TurretModRebirth
{
    public static final String ID = "sapturretmod";
    public static final String VERSION = "4.0.0-alpha.6";
    public static final Logger LOG = LogManager.getLogger(ID);
    public static final String CHANNEL = "SapTurretModNWCH";
    public static final String NAME = "Turret Mod Rebirth";
    public static final String GUI_FACTORY = "de.sanandrew.mods.turretmod.client.gui.config.TmrGuiFactory";
    public static final String DEPENDENCIES = "required-after:sanlib@[1.0.0,]";

    public static SimpleNetworkWrapper network;

    private static final String MOD_PROXY_CLIENT = "de.sanandrew.mods.turretmod.client.util.ClientProxy";
    private static final String MOD_PROXY_COMMON = "de.sanandrew.mods.turretmod.util.CommonProxy";

    @Mod.Instance(TurretModRebirth.ID)
    public static TurretModRebirth instance;
    @SidedProxy(modId = TurretModRebirth.ID, clientSide = TurretModRebirth.MOD_PROXY_CLIENT, serverSide = TurretModRebirth.MOD_PROXY_COMMON)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        event.getModMetadata().autogenerated = false;

        TmrConfiguration.initConfiguration(event);

        network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        PacketRegistry.initialize();
        Sounds.initialize();

        AmmoRegistry.INSTANCE.initialize();
        RepairKitRegistry.INSTANCE.initialize();
        TurretRegistry.INSTANCE.initialize();
        UpgradeRegistry.INSTANCE.initialize();
        ItemRegistry.initialize();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        TurretAssemblyRecipes.initialize();
        TileEntityElectrolyteGenerator.initializeRecipes();
        CraftingRecipes.initialize();

        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        TargetProcessor.initialize();

        proxy.postInit(event);
    }
}
