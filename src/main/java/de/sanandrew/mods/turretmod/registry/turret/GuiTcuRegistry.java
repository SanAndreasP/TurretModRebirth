/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.registry.turret;

import de.sanandrew.mods.turretmod.api.client.event.OpenTcuGuiEvent;
import de.sanandrew.mods.turretmod.api.client.tcu.IGuiTCU;
import de.sanandrew.mods.turretmod.api.client.tcu.IGuiTcuInst;
import de.sanandrew.mods.turretmod.api.event.OpenTcuContainerEvent;
import de.sanandrew.mods.turretmod.api.turret.IGuiTcuRegistry;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.client.gui.tcu.GuiTcuContainer;
import de.sanandrew.mods.turretmod.client.gui.tcu.GuiTcuScreen;
import de.sanandrew.mods.turretmod.client.gui.tcu.TargetType;
import de.sanandrew.mods.turretmod.client.gui.tcu.page.GuiInfo;
import de.sanandrew.mods.turretmod.client.gui.tcu.page.GuiLevels;
import de.sanandrew.mods.turretmod.client.gui.tcu.page.GuiShieldColorizer;
import de.sanandrew.mods.turretmod.client.gui.tcu.page.GuiSmartTargets;
import de.sanandrew.mods.turretmod.client.gui.tcu.page.GuiTargets;
import de.sanandrew.mods.turretmod.client.gui.tcu.page.GuiUpgrades;
import de.sanandrew.mods.turretmod.client.gui.tcu.page.PlayerHeads;
import de.sanandrew.mods.turretmod.entity.turret.UpgradeProcessor;
import de.sanandrew.mods.turretmod.inventory.container.ContainerTurretUpgrades;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.network.PacketSyncTcuGuis;
import de.sanandrew.mods.turretmod.registry.upgrades.UpgradeRegistry;
import de.sanandrew.mods.turretmod.registry.upgrades.Upgrades;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class GuiTcuRegistry
        implements IGuiTcuRegistry
{
    private static final String GUI_INFO           = "info";
    private static final String GUI_TARGETS_MOB    = "targets.creature";
    private static final String GUI_TARGETS_PLAYER = "targets.player";
    private static final String GUI_TARGETS_SMART  = "targets.smart";
    private static final String GUI_UPGRADES       = "upgrades";
    private static final String GUI_COLORIZER      = "colorizer";
    private static final String GUI_LEVELING       = "leveling";

    public static final List<String>   GUI_ENTRIES = new ArrayList<>();
    public static final GuiTcuRegistry INSTANCE = new GuiTcuRegistry();

    @SideOnly(Side.CLIENT)
    private static Map<String, GuiEntry> guis;
    private static Map<String, BiFunction<EntityPlayer, ITurretInst, Container>> containers;

    private GuiTcuRegistry() { }

    @SideOnly(Side.CLIENT)
    public Gui openGUI(int type, EntityPlayer player, ITurretInst turretInst) {
        if( type >= 0 && type < GUI_ENTRIES.size() ) {
            GuiEntry entry = getGuiEntry(GUI_ENTRIES.get(type));
            if( entry != null ) {
                OpenTcuGuiEvent event = new OpenTcuGuiEvent(player, turretInst, entry.factory);
                if( !MinecraftForge.EVENT_BUS.post(event) ) {
                    IGuiTCU guiDelegate = event.factory.get();
                    Container cnt = guiDelegate.getContainer(player, turretInst);
                    if( cnt != null ) {
                        return new GuiTcuContainer(GUI_ENTRIES.get(type), guiDelegate, cnt, turretInst);
                    } else {
                        return new GuiTcuScreen(GUI_ENTRIES.get(type), guiDelegate, turretInst);
                    }
                }
            }
        }

        return null;
    }

    public GuiEntry getGuiEntry(String location) {
        return guis.get(location);
    }

    public Container openContainer(int type, EntityPlayer player, ITurretInst turretInst) {
        if( type >= 0 && type < GUI_ENTRIES.size() ) {
            String key = GUI_ENTRIES.get(type);
            OpenTcuContainerEvent event = new OpenTcuContainerEvent(player, turretInst, containers.get(key));
            if( !MinecraftForge.EVENT_BUS.post(event) ) {
                if( event.factory != null ) {
                    return event.factory.apply(player, turretInst);
                }
            }
        }

        return null;
    }

    @Override
    public void registerGuiEntry(String key, int position, @Nullable BiFunction<EntityPlayer, ITurretInst, Container> containerFactory) {
        if( containers == null ) {
            containers = new HashMap<>();
        }

        if( position >= GUI_ENTRIES.size() ) {
            GUI_ENTRIES.add(key);
        } else {
            GUI_ENTRIES.set(position, key);
        }

        containers.put(key, containerFactory);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerGui(String key, ItemStack icon, Supplier<IGuiTCU> factory, Function<IGuiTcuInst<?>, Boolean> canShowTabFunc) {
        if( guis == null ) {
            guis = new HashMap<>();
        }

        guis.put(key, new GuiEntry(() -> icon, factory, canShowTabFunc));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerGui(String key, Supplier<ItemStack> iconSupplier, Supplier<IGuiTCU> factory, Function<IGuiTcuInst<?>, Boolean> canShowTabFunc) {
        if( guis == null ) {
            guis = new HashMap<>();
        }

        guis.put(key, new GuiEntry(iconSupplier, factory, canShowTabFunc));
    }

    public static void initializeEntries(IGuiTcuRegistry registry) {
        registry.registerGuiEntry(GUI_INFO, 0, null);
        registry.registerGuiEntry(GUI_TARGETS_MOB, 1, null);
        registry.registerGuiEntry(GUI_TARGETS_PLAYER, 2, null);
        registry.registerGuiEntry(GUI_TARGETS_SMART, 3, null);
        registry.registerGuiEntry(GUI_UPGRADES, 4, (player, turretInst) -> new ContainerTurretUpgrades(player.inventory, (UpgradeProcessor) turretInst.getUpgradeProcessor()));
        registry.registerGuiEntry(GUI_COLORIZER, 5, null);
        registry.registerGuiEntry(GUI_LEVELING, 6, null);
    }

    @SideOnly(Side.CLIENT)
    public static void initializeGuis(IGuiTcuRegistry registry) {
        registry.registerGui(GUI_INFO, new ItemStack(Items.BOOK), GuiInfo::new, null);
        registry.registerGui(GUI_TARGETS_MOB, new ItemStack(Items.SKULL, 1, 2), () -> new GuiTargets(TargetType.CREATURE), IGuiTcuInst::hasPermision);
        registry.registerGui(GUI_TARGETS_PLAYER, PlayerHeads::getRandomSkull, () -> new GuiTargets(TargetType.PLAYER), IGuiTcuInst::hasPermision);
        registry.registerGui(GUI_TARGETS_SMART, UpgradeRegistry.INSTANCE.getItem(Upgrades.SMART_TGT.getId()), GuiSmartTargets::new, GuiSmartTargets::showTab);
        registry.registerGui(GUI_UPGRADES, UpgradeRegistry.INSTANCE.getItem(UpgradeRegistry.EMPTY_UPGRADE.getId()), GuiUpgrades::new, IGuiTcuInst::hasPermision);
        registry.registerGui(GUI_COLORIZER, UpgradeRegistry.INSTANCE.getItem(Upgrades.SHIELD_COLORIZER.getId()), GuiShieldColorizer::new, GuiShieldColorizer::showTab);
        registry.registerGui(GuiTcuRegistry.GUI_LEVELING, UpgradeRegistry.INSTANCE.getItem(Upgrades.LEVELING.getId()), GuiLevels::new, GuiLevels::showTab);
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        if( event.getEntity() instanceof EntityPlayerMP && !event.getWorld().isRemote ) {
            PacketRegistry.sendToPlayer(new PacketSyncTcuGuis(), (EntityPlayerMP) event.getEntity());
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if( !event.getWorld().isRemote ) {
            PacketRegistry.sendToAll(new PacketSyncTcuGuis());
        }
    }

    @SideOnly(Side.CLIENT)
    public static final class GuiEntry
    {
        private final Supplier<ItemStack>               icon;
        private final Function<IGuiTcuInst<?>, Boolean> canShowTabFunc;
        private final Supplier<IGuiTCU>                 factory;

        private GuiEntry(Supplier<ItemStack> icon, Supplier<IGuiTCU> factory, Function<IGuiTcuInst<?>, Boolean> canShowTabFunc) {
            this.icon = icon;
            this.factory = factory;
            this.canShowTabFunc = canShowTabFunc;
        }

        public boolean showTab(IGuiTcuInst<?> gui) {
            return this.canShowTabFunc == null || this.canShowTabFunc.apply(gui);
        }

        public ItemStack getIcon() {
            return this.icon.get();
        }
    }
}
