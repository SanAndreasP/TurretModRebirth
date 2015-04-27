/**
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.network.packet;

import de.sanandrew.core.manpack.network.IPacket;
import de.sanandrew.core.manpack.util.javatuples.Tuple;
import de.sanandrew.core.manpack.util.javatuples.Unit;
import de.sanandrew.mods.turretmod.entity.turret.AEntityTurretBase;
import de.sanandrew.mods.turretmod.network.PacketManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.entity.Entity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;

import java.io.IOException;

public class PacketUpgradeListRequest
        implements IPacket
{
    @Override
    public void process(ByteBufInputStream inStream, ByteBuf rawData, INetHandler iNetHandler) throws IOException {
        if( iNetHandler instanceof NetHandlerPlayServer ) {
            int entityId = inStream.readInt();
            Entity e = ((NetHandlerPlayServer)iNetHandler).playerEntity.worldObj.getEntityByID(entityId);
            if( e instanceof AEntityTurretBase ) {
                PacketUpgradeList.sendPacket((AEntityTurretBase) e);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeData(ByteBufOutputStream outStream, Tuple data) throws IOException {
        outStream.writeInt((Integer) data.getValue(0));
    }

    public static void sendPacket(AEntityTurretBase turret) {
        Tuple data = Unit.with(turret.getEntityId());
        PacketManager.sendToServer(PacketManager.TURRET_UPGRADE_SYNC_REQUEST, data);
    }
}
