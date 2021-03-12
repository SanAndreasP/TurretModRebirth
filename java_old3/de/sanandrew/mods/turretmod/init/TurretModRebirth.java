/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.init;

import de.sanandrew.mods.sanlib.lib.network.MessageHandler;
import de.sanandrew.mods.turretmod.api.ITmrPlugin;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.TmrPlugin;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.registry.ammo.AmmunitionRegistry;
import de.sanandrew.mods.turretmod.registry.projectile.ProjectileRegistry;
import de.sanandrew.mods.turretmod.registry.repairkit.RepairKitRegistry;
import de.sanandrew.mods.turretmod.registry.turret.GuiTcuRegistry;
import de.sanandrew.mods.turretmod.registry.turret.TurretRegistry;
import de.sanandrew.mods.turretmod.registry.upgrades.UpgradeRegistry;
import de.sanandrew.mods.turretmod.util.TmrUtils;
import de.sanandrew.mods.turretmod.world.PlayerList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

//@Mod(modid = TmrConstants.ID, version = TmrConstants.VERSION, name = TmrConstants.NAME, guiFactory = TurretModRebirth.GUI_FACTORY, dependencies = TmrConstants.DEPENDENCIES)
@Mod(TmrConstants.ID)
public class TurretModRebirth
{
    public static final List<ITmrPlugin> PLUGINS          = new ArrayList<>();
//    static final         String               GUI_FACTORY      = "de.sanandrew.mods.turretmod.client.gui.config.TmrGuiFactory";
//    private static final String               MOD_PROXY_CLIENT = "de.sanandrew.mods.turretmod.client.init.ClientProxy";
//    private static final String               MOD_PROXY_COMMON = "de.sanandrew.mods.turretmod.init.CommonProxy";
    public static       MessageHandler   network = new MessageHandler(TmrConstants.ID, "1.0.0");
//    @Mod.Instance(TmrConstants.ID)
//    public static       TurretModRebirth instance;
//    @SidedProxy(modId = TmrConstants.ID, clientSide = TurretModRebirth.MOD_PROXY_CLIENT, serverSide = TurretModRebirth.MOD_PROXY_COMMON)
//    public static        CommonProxy          proxy;

//    private boolean isDev;

    public TurretModRebirth() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::constructMod);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupCommon);
    }

    private void constructMod(FMLConstructModEvent event) {
//        this.isDev = ModList.get().getModFileById(TmrConstants.ID).getFile().getFilePath().toFile().isDirectory();

        loadPlugins(ModList.get().getAllScanData());

//        ModLoadingContext.get().registerConfig();

        PacketRegistry.initialize();

        PLUGINS.forEach(plugin -> plugin.preSetup(TmrUtils.INSTANCE));
        PLUGINS.forEach(plugin -> plugin.registerTurrets(TurretRegistry.INSTANCE));
        PLUGINS.forEach(plugin -> plugin.registerAmmo(AmmunitionRegistry.INSTANCE));
        PLUGINS.forEach(plugin -> plugin.registerRepairKits(RepairKitRegistry.INSTANCE));
        PLUGINS.forEach(plugin -> plugin.registerUpgrades(UpgradeRegistry.INSTANCE));
        PLUGINS.forEach(plugin -> plugin.registerProjectiles(ProjectileRegistry.INSTANCE));
    }

    private void setupCommon(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(PlayerList.INSTANCE);

        TurretModRebirth.PLUGINS.forEach(plugin -> plugin.registerTcuEntries(GuiTcuRegistry.INSTANCE));
    }

//    @Mod.EventHandler
//    public void preInit(FMLPreInitializationEvent event) {
//        // fix for mod options config button being disabled
//        event.getModMetadata().autogenerated = false;
//        this.isDev = event.getSourceFile().isDirectory();
//
//        loadPlugins(event.getAsmData());
//
//        TmrConfig.initConfiguration(event);
//
//        network = NetworkRegistry.INSTANCE.newSimpleChannel(TmrConstants.CHANNEL);
//        PacketRegistry.initialize();
//
//        PLUGINS.forEach(plugin -> plugin.preInit(TmrUtils.INSTANCE));
//        PLUGINS.forEach(plugin -> plugin.registerTurrets(TurretRegistry.INSTANCE));
//        PLUGINS.forEach(plugin -> plugin.registerAmmo(AmmunitionRegistry.INSTANCE));
//        PLUGINS.forEach(plugin -> plugin.registerRepairKits(RepairKitRegistry.INSTANCE));
//        PLUGINS.forEach(plugin -> plugin.registerUpgrades(UpgradeRegistry.INSTANCE));
//        PLUGINS.forEach(plugin -> plugin.registerProjectiles(ProjectileRegistry.INSTANCE));
//
//        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
//
//        proxy.preInit(event);
//    }

    private static void loadPlugins(List<ModFileScanData> dataTable) {
        Type tmrPluginType = Type.getType(TmrPlugin.class);
        for( ModFileScanData scanData : dataTable ) {
            Set<ModFileScanData.AnnotationData> annotationDataSet = scanData.getAnnotations();
            for( ModFileScanData.AnnotationData annotationData : annotationDataSet ) {
                if( Objects.equals(annotationData.getAnnotationType(), tmrPluginType) ) {
                    try {
                        Class<?>                    asmClass         = Class.forName(annotationData.getMemberName());
                        Class<? extends ITmrPlugin> asmInstanceClass = asmClass.asSubclass(ITmrPlugin.class);
                        ITmrPlugin                  instance         = asmInstanceClass.getConstructor().newInstance();
                        PLUGINS.add(instance);
                    } catch( ClassNotFoundException | IllegalAccessException | ExceptionInInitializerError | InstantiationException | NoSuchMethodException | InvocationTargetException e ) {
                        TmrConstants.LOG.error("Failed to load: {}", annotationData.getMemberName(), e);
                    }
                }
            }
        }

        PLUGINS.sort((p1, p2) -> p1 instanceof TmrInternalPlugin ? -1 : p2 instanceof TmrInternalPlugin ? 1 : 0);
    }

//    @Mod.EventHandler
//    public void init(FMLInitializationEvent event) {
//        PLUGINS.forEach(plugin -> {
//            plugin.registerAssemblyRecipes(AssemblyManager.INSTANCE);
//            plugin.registerElectrolyteRecipes(ElectrolyteManager.INSTANCE);
//        });
//
//        CTHandler.onInit();
//
//        proxy.init(event);
//    }
//
//    @Mod.EventHandler
//    public void postInit(FMLPostInitializationEvent event) {
//        PLUGINS.forEach(ITmrPlugin::postInit);
//
//        proxy.postInit(event);
//
//        AssemblyManager.INSTANCE.finalizeRegistry();
//
//        TargetList.initializePostInit();
//    }
//
//    @Mod.EventHandler
//    public void interModComm(FMLInterModComms.IMCEvent event) {
//        event.getMessages().forEach(message -> {
//            if( message.key.equals(TmrConstants.ID + ":checkProjForShield") && message.isFunctionMessage() ) {
//                message.getFunctionValue(Entity.class, Entity.class).ifPresent(ForcefieldHandler.PROJ_GET_OWNER::add);
//            }
//        });
//    }
//
//    // for debugging purposes only!
//    @Mod.EventHandler
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    public void loadComplete(net.minecraftforge.fml.common.event.FMLLoadCompleteEvent event) {
//        if( this.isDev ) {
//            java.util.function.Function<Object, Long> gos = (obj -> {
//                try {
//                    Class cls = Class.forName("jdk.nashorn.internal.ir.debug.ObjectSizeCalculator");
//                    return de.sanandrew.mods.sanlib.lib.util.ReflectionUtils.<Long, Object>invokeCachedMethod(cls, null, "getObjectSize", "getObjectSize",
//                                                                                                              new Class[] { Object.class },
//                                                                                                              new Object[] { obj });
//                } catch( ClassNotFoundException ex ) {
//                    return 0L;
//                }
//            });
//
//            TmrConstants.LOG.log(org.apache.logging.log4j.Level.INFO, String.format("Memory used by turret registry: %d bytes", gos.apply(TurretRegistry.INSTANCE)));
//            TmrConstants.LOG.log(org.apache.logging.log4j.Level.INFO, String.format("Memory used by ammo registry: %d bytes", gos.apply(AmmunitionRegistry.INSTANCE)));
//            TmrConstants.LOG.log(org.apache.logging.log4j.Level.INFO, String.format("Memory used by upgrade registry: %d bytes", gos.apply(UpgradeRegistry.INSTANCE)));
//            TmrConstants.LOG.log(org.apache.logging.log4j.Level.INFO, String.format("Memory used by repairkit registry: %d bytes", gos.apply(RepairKitRegistry.INSTANCE)));
//        }
//    }
}
