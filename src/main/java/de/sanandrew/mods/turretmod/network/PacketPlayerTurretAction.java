/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.network;

import de.sanandrew.mods.sanlib.lib.Tuple;
import de.sanandrew.mods.sanlib.lib.network.AbstractMessage;
import de.sanandrew.mods.sanlib.lib.util.InventoryUtils;
import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.entity.turret.TargetProcessor;
import de.sanandrew.mods.turretmod.entity.turret.UpgradeProcessor;
import de.sanandrew.mods.turretmod.registry.turret.TurretRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;

public class PacketPlayerTurretAction
        extends AbstractMessage<PacketPlayerTurretAction>
{
    public static final byte SET_ACTIVE = 0;
    private static final byte DISMANTLE = 1;
    public static final byte SET_DEACTIVE = 2;

    private int turretId;
    private byte actionId;

    @SuppressWarnings("unused")
    public PacketPlayerTurretAction() { }

    public PacketPlayerTurretAction(ITurretInst turretInst, byte action) {
        this.turretId = turretInst.get().getEntityId();
        this.actionId = action;
    }

    @Override
    public void handleClientMessage(PacketPlayerTurretAction packet, EntityPlayer player) { }

    @Override
    public void handleServerMessage(PacketPlayerTurretAction packet, EntityPlayer player) {
        Entity e = player.world.getEntityByID(packet.turretId);
        if( e instanceof ITurretInst) {
            ITurretInst turretInst = (ITurretInst) e;
            if( !turretInst.hasPlayerPermission(player) ) {
                return;
            }

            switch( packet.actionId ) {
                case DISMANTLE:
                    tryDismantle(player, turretInst);
                    break;
                case SET_ACTIVE:
                    turretInst.setActive(true);
                    break;
                case SET_DEACTIVE:
                    turretInst.setActive(false);
                    break;
            }
        }
    }

    public static boolean tryDismantle(EntityPlayer player, ITurretInst turretInst) {
        Tuple chestItm = InventoryUtils.getSimilarStackFromInventory(new ItemStack(Blocks.CHEST), player.inventory, true);
        if( chestItm != null && ItemStackUtils.isValid(chestItm.getValue(1)) ) {
            ItemStack chestStack = chestItm.getValue(1);
            EntityLiving turretL = turretInst.get();
            if( turretL.world.isRemote ) {
                PacketRegistry.sendToServer(new PacketPlayerTurretAction(turretInst, PacketPlayerTurretAction.DISMANTLE));
                return true;
            } else {
                BlockPos chestPos = turretL.getPosition();
                if( turretL.world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 3) ) {
                    TileEntity te = turretL.world.getTileEntity(chestPos);

                    if( te instanceof TileEntityChest ) {
                        TileEntityChest chest = (TileEntityChest) te;
                        chest.setInventorySlotContents(0, TurretRegistry.INSTANCE.getItem(turretInst));
                        ((TargetProcessor) turretInst.getTargetProcessor()).putAmmoInInventory(chest);

                        chestStack.shrink(1);
                        if( chestStack.getCount() < 1 ) {
                            player.inventory.setInventorySlotContents(chestItm.getValue(0), ItemStackUtils.getEmpty());
                        } else {
                            player.inventory.setInventorySlotContents(chestItm.getValue(0), chestStack.copy());
                        }
                        player.inventoryContainer.detectAndSendChanges();
                        //TODO: make custom container for turrets and put upgrades in it
                        ((UpgradeProcessor) turretInst.getUpgradeProcessor()).dropUpgrades();
                        turretL.onKillCommand();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.turretId = buf.readInt();
        this.actionId = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.turretId);
        buf.writeByte(this.actionId);
    }
}
