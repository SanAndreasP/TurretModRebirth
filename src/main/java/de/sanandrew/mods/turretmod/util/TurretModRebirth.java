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
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.TmrPlugin;
import de.sanandrew.mods.turretmod.entity.turret.TargetProcessor;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.registry.ammo.AmmunitionRegistry;
import de.sanandrew.mods.turretmod.registry.assembly.TurretAssemblyRegistry;
import de.sanandrew.mods.turretmod.registry.electrolytegen.ElectrolyteRegistry;
import de.sanandrew.mods.turretmod.registry.projectile.ProjectileRegistry;
import de.sanandrew.mods.turretmod.registry.repairkit.RepairKitRegistry;
import de.sanandrew.mods.turretmod.registry.turret.TurretRegistry;
import de.sanandrew.mods.turretmod.registry.turret.shieldgen.ShieldHandler;
import de.sanandrew.mods.turretmod.registry.upgrades.UpgradeRegistry;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod(modid = TmrConstants.ID, version = TmrConstants.VERSION, name = TmrConstants.NAME, guiFactory = TurretModRebirth.GUI_FACTORY, dependencies = TmrConstants.DEPENDENCIES)
public class TurretModRebirth
{
    private static final String GUI_FACTORY = "de.sanandrew.mods.turretmod.client.gui.config.TmrGuiFactory";

    public static SimpleNetworkWrapper network;

    private static final String MOD_PROXY_CLIENT = "de.sanandrew.mods.turretmod.client.util.ClientProxy";
    private static final String MOD_PROXY_COMMON = "de.sanandrew.mods.turretmod.util.CommonProxy";

    public static final List<ITmrPlugin> PLUGINS = new ArrayList<>();

    @Mod.Instance(TmrConstants.ID)
    public static TurretModRebirth instance;
    @SidedProxy(modId = TmrConstants.ID, clientSide = TurretModRebirth.MOD_PROXY_CLIENT, serverSide = TurretModRebirth.MOD_PROXY_COMMON)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // fix for mod options config button being disabled
        event.getModMetadata().autogenerated = false;

        loadPlugins(event.getAsmData());

        TmrConfig.initConfiguration(event);

        network = NetworkRegistry.INSTANCE.newSimpleChannel(TmrConstants.CHANNEL);
        PacketRegistry.initialize();

        PLUGINS.forEach(plugin -> plugin.preInit(TmrUtils.INSTANCE));
        PLUGINS.forEach(plugin -> plugin.registerTurrets(TurretRegistry.INSTANCE));
        PLUGINS.forEach(plugin -> plugin.registerAmmo(AmmunitionRegistry.INSTANCE));
        PLUGINS.forEach(plugin -> plugin.registerRepairKits(RepairKitRegistry.INSTANCE));
        PLUGINS.forEach(plugin -> plugin.registerUpgrades(UpgradeRegistry.INSTANCE));
        PLUGINS.forEach(plugin -> plugin.registerProjectiles(ProjectileRegistry.INSTANCE));

        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PLUGINS.forEach(plugin -> plugin.registerAssemblyRecipes(TurretAssemblyRegistry.INSTANCE));

        ElectrolyteRegistry.initialize();

        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PLUGINS.forEach(ITmrPlugin::postInit);

        TargetProcessor.initialize();

        proxy.postInit(event);

        TurretAssemblyRegistry.INSTANCE.finalizeRegistry();
    }

    @Mod.EventHandler
    public void interModComm(FMLInterModComms.IMCEvent event) {
        event.getMessages().forEach(message -> {
            if( message.key.equals(TmrConstants.ID + ":checkProjForShield") && message.isFunctionMessage() ) {
                message.getFunctionValue(Entity.class, Entity.class).ifPresent(ShieldHandler.PROJ_GET_OWNER::add);
            }
        });
    }

    /* // for debugging purposes only!
    @Mod.EventHandler
    public void loadComplete(net.minecraftforge.fml.common.event.FMLLoadCompleteEvent event) {
        java.util.function.Function<Object, Long> getObjSize = (obj -> {
            try {
                Class cls = Class.forName("jdk.nashorn.internal.ir.debug.ObjectSizeCalculator");
                return de.sanandrew.mods.sanlib.lib.util.ReflectionUtils.<Long, Object>invokeCachedMethod(cls, null, "getObjectSize", "getObjectSize", new Class[] {Object.class},  new Object[] {obj});
            } catch( ClassNotFoundException ex ) {
                return 0L;
            }
        });

        TmrConstants.LOG.log(org.apache.logging.log4j.Level.INFO, String.format("Memory used by turret registry: %d bytes", getObjSize.apply(TurretRegistry.INSTANCE)));
        TmrConstants.LOG.log(org.apache.logging.log4j.Level.INFO, String.format("Memory used by ammo registry: %d bytes", getObjSize.apply(TurretAmmoRegistry.INSTANCE)));
        TmrConstants.LOG.log(org.apache.logging.log4j.Level.INFO, String.format("Memory used by upgrade registry: %d bytes", getObjSize.apply(UpgradeRegistry.INSTANCE)));
        TmrConstants.LOG.log(org.apache.logging.log4j.Level.INFO, String.format("Memory used by repairkit registry: %d bytes", getObjSize.apply(RepairKitRegistry.INSTANCE)));
    } */

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
                TmrConstants.LOG.error("Failed to load: {}", asmData.getClassName(), e);
            }
        }

        // make sure this mods internal plungin is loaded first. Always.
        PLUGINS.sort((p1, p2) -> p1 instanceof TmrInternalPlugin ? -1 : p2 instanceof TmrInternalPlugin ? 1 : 0);
    }
}
