/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.util;

import de.sanandrew.mods.sanlib.lib.Tuple;
import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.EnumGui;
import de.sanandrew.mods.turretmod.api.turret.EntityTurret;
import de.sanandrew.mods.turretmod.entity.turret.EntityTurretLaser;
import de.sanandrew.mods.turretmod.entity.turret.UpgradeProcessor;
import de.sanandrew.mods.turretmod.inventory.ContainerAssemblyFilter;
import de.sanandrew.mods.turretmod.inventory.ContainerElectrolyteGenerator;
import de.sanandrew.mods.turretmod.inventory.ContainerTurretAssembly;
import de.sanandrew.mods.turretmod.inventory.ContainerTurretUpgrades;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import de.sanandrew.mods.turretmod.network.PacketOpenGui;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.tileentity.TileEntityElectrolyteGenerator;
import de.sanandrew.mods.turretmod.tileentity.TileEntityTurretAssembly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.apache.logging.log4j.Level;

public class CommonProxy
        implements IGuiHandler
{
    public static int entityCount = 0;

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(PlayerList.INSTANCE);
    }

    public void init(FMLInitializationEvent event) { }

    public void postInit(FMLPostInitializationEvent event) { }

    @Override
    public final Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if( id >= 0 && id < EnumGui.VALUES.length ) {
            TileEntity te;
            switch( EnumGui.VALUES[id] ) {
                case GUI_TCU_UPGRADES: {
                    Entity e = world.getEntityByID(x);
                    if( e instanceof EntityTurret ) {
                        return new ContainerTurretUpgrades(player.inventory, ((UpgradeProcessor) ((EntityTurret) e).getUpgradeProcessor()));
                    }
                    break;
                }
                case GUI_TASSEMBLY_MAN:
                    te = world.getTileEntity(new BlockPos(x, y, z));
                    if( te instanceof TileEntityTurretAssembly ) {
                        return new ContainerTurretAssembly(player.inventory, (TileEntityTurretAssembly) te);
                    }
                    break;
                case GUI_TASSEMBLY_FLT:
                    ItemStack stack = player.getHeldItemMainhand();
                    if( ItemStackUtils.isValid(stack) && stack.getItem() == ItemRegistry.assembly_upg_filter ) {
                        return new ContainerAssemblyFilter(player.inventory, stack, player.inventory.currentItem);
                    }
                    break;
                case GUI_POTATOGEN:
                    te = world.getTileEntity(new BlockPos(x, y, z));
                    if( te instanceof TileEntityElectrolyteGenerator ) {
                        return new ContainerElectrolyteGenerator(player.inventory, (TileEntityElectrolyteGenerator) te);
                    }

            }
        } else {
            TmrConstants.LOG.log(Level.WARN, "Gui ID %d cannot be opened as it isn't a valid index in EnumGui!", id);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void openGui(EntityPlayer player, EnumGui id, int x, int y, int z) {
        int guiId = id.ordinal();

        if( player instanceof EntityPlayerMP ) {
            if( getServerGuiElement(guiId, player, player.world, x, y, z) == null ) {
                PacketRegistry.sendToPlayer(new PacketOpenGui((byte) guiId, x, y, z), (EntityPlayerMP) player);
            } else {
                FMLNetworkHandler.openGui(player, TurretModRebirth.instance, guiId, player.world, x, y, z);
            }
        } else {
            if( getServerGuiElement(guiId, player, player.world, x, y, z) == null ) {
                FMLNetworkHandler.openGui(player, TurretModRebirth.instance, guiId, player.world, x, y, z);
            } else {
                PacketRegistry.sendToServer(new PacketOpenGui((byte) guiId, x, y, z));
            }
        }
    }

    public void spawnParticle(EnumParticle particle, double x, double y, double z) {
        this.spawnParticle(particle, x, y, z, null);
    }

    public void spawnParticle(EnumParticle particle, double x, double y, double z, Tuple data) { }

    public void playTurretLaser(EntityTurretLaser turretLaser) { }
}