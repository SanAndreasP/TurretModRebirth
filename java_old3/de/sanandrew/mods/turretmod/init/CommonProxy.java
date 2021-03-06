/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.init;

import de.sanandrew.mods.sanlib.lib.Tuple;
import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.sanlib.lib.util.PlayerUtils;
import de.sanandrew.mods.turretmod.api.EnumGui;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.turret.IForcefieldProvider;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.entity.turret.EntityTurret;
import de.sanandrew.mods.turretmod.entity.turret.EntityTurretProjectile;
import de.sanandrew.mods.turretmod.inventory.container.ContainerAssemblyFilter;
import de.sanandrew.mods.turretmod.inventory.container.ContainerCartridge;
import de.sanandrew.mods.turretmod.inventory.container.ContainerElectrolyteGenerator;
import de.sanandrew.mods.turretmod.inventory.container.ContainerTurretAssembly;
import de.sanandrew.mods.turretmod.inventory.container.ContainerTurretCrate;
import de.sanandrew.mods.turretmod.item.ItemAmmoCartridge;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import de.sanandrew.mods.turretmod.network.PacketOpenGui;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.registry.EnumEffect;
import de.sanandrew.mods.turretmod.registry.turret.GuiTcuRegistry;
import de.sanandrew.mods.turretmod.tileentity.TileEntityTurretCrate;
import de.sanandrew.mods.turretmod.tileentity.assembly.TileEntityTurretAssembly;
import de.sanandrew.mods.turretmod.tileentity.electrolytegen.TileEntityElectrolyteGenerator;
import de.sanandrew.mods.turretmod.world.PlayerList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.logging.log4j.Level;

public class CommonProxy
        implements IGuiHandler
{
    public void preInit(FMLPreInitializationEvent event) {
//        MinecraftForge.EVENT_BUS.register(PlayerList.INSTANCE);

//        EntityRegistry.registerModEntity(new ResourceLocation(TmrConstants.ID, "turret"), EntityTurret.class, TmrConstants.ID + ".turret", 0, TurretModRebirth.instance, 128, 1, true);
//        EntityRegistry.registerModEntity(new ResourceLocation(TmrConstants.ID, "projectile"), EntityTurretProjectile.class, TmrConstants.ID + ".projectile", 1, TurretModRebirth.instance, 128, 1, true);
    }

    public void init(FMLInitializationEvent event) {
        TurretModRebirth.PLUGINS.forEach(plugin -> plugin.registerTcuEntries(GuiTcuRegistry.INSTANCE));
    }

    public void postInit(FMLPostInitializationEvent event) { }

    public boolean checkTurretGlowing(ITurretInst turret) {
        return false;
    }

    @Override
    public final Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if( id >= 0 && id < EnumGui.VALUES.length ) {
            TileEntity te;
            switch( EnumGui.VALUES[id] ) {
                case TCU:
                    Entity e = world.getEntityByID(x);
                    if( e instanceof ITurretInst ) {
                        return GuiTcuRegistry.INSTANCE.openContainer(y, player, (ITurretInst) e, z == 1);
                    }
                    break;
                case TASSEMBLY:
                    te = world.getTileEntity(new BlockPos(x, y, z));
                    if( te instanceof TileEntityTurretAssembly ) {
                        return new ContainerTurretAssembly(player.inventory, (TileEntityTurretAssembly) te);
                    }
                    break;
                case TASSEMBLY_FLT:
                    ItemStack stack = PlayerUtils.getHeldItemOfType(player, ItemRegistry.ASSEMBLY_UPG_FILTER);
                    if( ItemStackUtils.isValid(stack) ) {
                        return new ContainerAssemblyFilter(player.inventory, stack, player.inventory.currentItem);
                    }
                    break;
                case ELECTROLYTE_GENERATOR:
                    te = world.getTileEntity(new BlockPos(x, y, z));
                    if( te instanceof TileEntityElectrolyteGenerator ) {
                        return new ContainerElectrolyteGenerator(player.inventory, (TileEntityElectrolyteGenerator) te);
                    }
                    break;
                case CARTRIDGE:
                    ItemStack heldStack = PlayerUtils.getHeldItemOfType(player, ItemRegistry.AMMO_CARTRIDGE);
                    if( ItemStackUtils.isValid(heldStack) ) {
                        IInventory inv = ItemAmmoCartridge.getInventory(heldStack);
                        if( inv != null ) {
                            return new ContainerCartridge(player.inventory, inv, player);
                        }
                    }
                    break;
                case TCRATE:
                    te = world.getTileEntity(new BlockPos(x, y, z));
                    if( te instanceof TileEntityTurretCrate ) {
                        return new ContainerTurretCrate(player.inventory, (TileEntityTurretCrate) te);
                    }
                    break;
            }
        } else {
            TmrConstants.LOG.log(Level.WARN, String.format("Gui ID %d cannot be opened as it isn't a valid index in EnumGui!", id));
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

    public void addEffect(EnumEffect effect, double x, double y, double z, Tuple data) { }

    public void playTurretLaser(ITurretInst turretInst) { }

    public void addForcefield(Entity e, IForcefieldProvider provider) { }

    public boolean hasForcefield(Entity e, Class<? extends IForcefieldProvider> providerCls) {
        return false;
    }

    public boolean isPlayerPressingShift() {
        return false;
    }
}
