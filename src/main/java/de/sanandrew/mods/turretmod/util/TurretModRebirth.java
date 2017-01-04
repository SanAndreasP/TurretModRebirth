/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.util;

import de.sanandrew.mods.turretmod.api.ITmrPlugin;
import de.sanandrew.mods.turretmod.api.TmrPlugin;
import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.entity.turret.TargetProcessor;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.registry.ammo.AmmoRegistry;
import de.sanandrew.mods.turretmod.registry.assembly.TurretAssemblyRegistry;
import de.sanandrew.mods.turretmod.registry.medpack.RepairKitRegistry;
import de.sanandrew.mods.turretmod.registry.turret.TurretRegistry;
import de.sanandrew.mods.turretmod.registry.upgrades.UpgradeRegistry;
import de.sanandrew.mods.turretmod.tileentity.TileEntityElectrolyteGenerator;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod(modid = TurretModRebirth.ID, version = TurretModRebirth.VERSION, name = TurretModRebirth.NAME, guiFactory = TurretModRebirth.GUI_FACTORY, dependencies = TurretModRebirth.DEPENDENCIES)
public class TurretModRebirth
{
    public static final String ID = "sapturretmod";
    public static final String VERSION = "4.0.0-alpha.6";
    public static final Logger LOG = LogManager.getLogger(ID);
    public static final String CHANNEL = "SapTurretModNWCH";
    public static final String NAME = "Turret Mod Rebirth";
    public static final String GUI_FACTORY = "de.sanandrew.mods.turretmod.client.gui.config.TmrGuiFactory";
    public static final String DEPENDENCIES = "required-after:Forge@[12.18.2.2099,];required-after:sanlib@[1.0.0,]";

    public static SimpleNetworkWrapper network;

    private static final String MOD_PROXY_CLIENT = "de.sanandrew.mods.turretmod.client.util.ClientProxy";
    private static final String MOD_PROXY_COMMON = "de.sanandrew.mods.turretmod.util.CommonProxy";

    private static final List<ITmrPlugin> PLUGINS = new ArrayList<>();

    @Mod.Instance(TurretModRebirth.ID)
    public static TurretModRebirth instance;
    @SidedProxy(modId = TurretModRebirth.ID, clientSide = TurretModRebirth.MOD_PROXY_CLIENT, serverSide = TurretModRebirth.MOD_PROXY_COMMON)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        event.getModMetadata().autogenerated = false;

        loadPlugins(event.getAsmData());

        TmrConfiguration.initConfiguration(event);

        network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        PacketRegistry.initialize();
        Sounds.initialize();

        AmmoRegistry.INSTANCE.initialize();
        RepairKitRegistry.INSTANCE.initialize();
        TurretRegistry.INSTANCE.initialize();
        UpgradeRegistry.INSTANCE.initialize();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PLUGINS.forEach(plugin -> {
            plugin.registerAssemblyRecipes(TurretAssemblyRegistry.INSTANCE);
        });
        TileEntityElectrolyteGenerator.initializeRecipes();
        CraftingRecipes.initialize();

        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        TargetProcessor.initialize();

        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void onMissingMappings(FMLMissingMappingsEvent event) {
        remap(event, BlockRegistry.electrolyte_generator, "sapturretmod:potato_generator");
        remap(event, ItemRegistry.repair_kit, "sapturretmod:turret_repair_kit");
        remap(event, ItemRegistry.assembly_upg_auto, "sapturretmod:turret_assembly_auto");
        remap(event, ItemRegistry.assembly_upg_speed, "sapturretmod:turret_assembly_speed");
        remap(event, ItemRegistry.assembly_upg_filter, "sapturretmod:turret_assembly_filter");
    }

    private static void remap(FMLMissingMappingsEvent event, final Block block, final String oldName) {
        event.get().stream().filter(mapping -> mapping != null && mapping.name.equals(oldName) && mapping.type == GameRegistry.Type.BLOCK).forEach(mapping -> {
            mapping.remap(block);
        });
        Item itm = Item.getItemFromBlock(block);
        if( itm != null ) {
            remap(event, itm, oldName);
        }
    }

    private static void remap(FMLMissingMappingsEvent event, final Item item, final String oldName) {
        event.get().stream().filter(mapping -> mapping != null && mapping.name.equals(oldName) && mapping.type == GameRegistry.Type.ITEM).forEach(mapping -> {
            mapping.remap(item);
        });
    }

    private static void loadPlugins(ASMDataTable dataTable) {
        String annotationClassName = TmrPlugin.class.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = dataTable.getAll(annotationClassName);
        for (ASMDataTable.ASMData asmData : asmDatas) {
            try {
                Class<?> asmClass = Class.forName(asmData.getClassName());
                Class<? extends ITmrPlugin> asmInstanceClass = asmClass.asSubclass(ITmrPlugin.class);
                ITmrPlugin instance = asmInstanceClass.getConstructor().newInstance();
                PLUGINS.add(instance);
            } catch (ClassNotFoundException | IllegalAccessException | ExceptionInInitializerError | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                LOG.error("Failed to load: {}", asmData.getClassName(), e);
            }
        }
    }
}
