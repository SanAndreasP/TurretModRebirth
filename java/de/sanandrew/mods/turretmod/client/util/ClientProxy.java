/**
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.util;

import de.sanandrew.core.manpack.util.helpers.SAPUtils;
import de.sanandrew.mods.turretmod.client.gui.GuiTurretCtrlUnit;
import de.sanandrew.mods.turretmod.entity.turret.AEntityTurretBase;
import de.sanandrew.mods.turretmod.util.CommonProxy;
import de.sanandrew.mods.turretmod.util.EnumGui;
import de.sanandrew.mods.turretmod.util.TmrEntities;
import de.sanandrew.mods.turretmod.util.TurretMod;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientProxy
        extends CommonProxy
{
    @Override
    public void init() {
        super.init();

        TmrEntities.registerRenderers();
    }

    @Override
    public void processTargetListClt(ByteBufInputStream stream) throws IOException {
        int entityId = stream.readInt();
        int listSize = stream.readInt();
        Entity e = getMinecraft().theWorld.getEntityByID(entityId);
        if( e instanceof AEntityTurretBase ) {
            List<Class<?extends EntityLiving>> applicableTargets = new ArrayList<>(listSize);
            for( int i = 0; i < listSize; i++ ) {
                    String clsName = stream.readUTF();
                    //noinspection unchecked
                    applicableTargets.add((Class<? extends EntityLiving>) EntityList.stringToClassMapping.get(clsName));
            }

            ((AEntityTurretBase) e).setTargetList(applicableTargets);
        }
    }

    @Override
    public void openGui(EntityPlayer player, EnumGui id, int x, int y, int z) {
        if( player == null ) {
            player = Minecraft.getMinecraft().thePlayer;
        }

        super.openGui(player, id, x, y, z);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if( SAPUtils.isIndexInRange(EnumGui.VALUES, id) ) {
            switch( EnumGui.VALUES[id] ) {
                case GUI_TCU_PG1:
                    return new GuiTurretCtrlUnit((AEntityTurretBase) getMinecraft().theWorld.getEntityByID(x));
            }
        } else {
            TurretMod.MOD_LOG.printf(Level.WARN, "Gui ID %d cannot be opened as it isn't a valid index in EnumGui!", id);
        }

        return null;
    }

    private static Minecraft getMinecraft() {
        return Minecraft.getMinecraft();
    }
}
