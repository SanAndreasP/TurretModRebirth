/**
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.gui.tcu;

import de.sanandrew.mods.turretmod.client.gui.control.GuiItemTab;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import de.sanandrew.mods.turretmod.util.EnumGui;
import de.sanandrew.mods.turretmod.util.Lang;
import de.sanandrew.mods.turretmod.util.TurretModRebirth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiTCUHelper
{
    public static final int X_SIZE = 176;
    public static final int Y_SIZE = 222;
    public static GuiItemTab pageInfo;
    public static GuiItemTab pageEntityTargets;
    public static GuiItemTab pagePlayerTargets;
    public static GuiItemTab pageUpgrades;

    @SuppressWarnings("unchecked")
    public static void initGui(GuiTurretCtrlUnit gui) {
        List btnList = gui.getButtonList();
        btnList.add(pageInfo = new GuiItemTab(btnList.size(), gui.getGuiLeft() - 23, gui.getGuiTop() + 5,
                new ItemStack(Items.sign), Lang.translate(Lang.TCU_PAGE_TAB, "info"), false));
        btnList.add(pageEntityTargets = new GuiItemTab(btnList.size(), gui.getGuiLeft() - 23, gui.getGuiTop() + 33,
                new ItemStack(Items.skull, 1, 2), Lang.translate(Lang.TCU_PAGE_TAB, "targetsEntity"), false));
        btnList.add(pagePlayerTargets = new GuiItemTab(btnList.size(), gui.getGuiLeft() - 23, gui.getGuiTop() + 61,
                new ItemStack(Items.skull, 1, 3), Lang.translate(Lang.TCU_PAGE_TAB, "targetsPlayer"), false));
        btnList.add(pageUpgrades = new GuiItemTab(btnList.size(), gui.getGuiLeft() - 23, gui.getGuiTop() + 89,
                new ItemStack(ItemRegistry.turretUpgrade), Lang.translate(Lang.TCU_PAGE_TAB, "upgrades"), false));
    }

    public static void drawScreen(GuiTurretCtrlUnit gui) {
        GL11.glColor3f(1.0F, 1.0F, 1.0F);

        FontRenderer fRender = gui.getFontRenderer();
        String pageName = "";
        if( !pageInfo.enabled ) {
            pageName = "info";
        } else if( !pageEntityTargets.enabled ) {
            pageName = "targetsEntity";
        } else if( !pagePlayerTargets.enabled ) {
            pageName = "targetsPlayer";
        } else if( !pageUpgrades.enabled ) {
            pageName = "upgrades";
        }
        pageName = Lang.translate(String.format(Lang.TCU_PAGE_TITLE, pageName));
        fRender.drawString(pageName, gui.getGuiLeft() + 8, gui.getGuiTop() + 6, 0x404040);

        String turretName = Lang.translate(Lang.translateEntityCls(gui.getTurret().getClass()));
        fRender.drawString(turretName, gui.getGuiLeft() + (X_SIZE - fRender.getStringWidth(turretName)) / 2, gui.getGuiTop() + Y_SIZE - 15, 0x00FF00, false);
    }

    public static boolean actionPerformed(GuiButton button, GuiTurretCtrlUnit gui) {
        Minecraft mc = gui.getMc();
        if( button == pageInfo ) {
            TurretModRebirth.proxy.openGui(mc.thePlayer, EnumGui.GUI_TCU_INFO, gui.getTurret().getEntityId(), 0, 0);
            return true;
        } else if( button == pageEntityTargets ) {
            TurretModRebirth.proxy.openGui(mc.thePlayer, EnumGui.GUI_TCU_ENTITY_TARGETS, gui.getTurret().getEntityId(), 0, 0);
            return true;
        } else if( button == pagePlayerTargets ) {
            TurretModRebirth.proxy.openGui(mc.thePlayer, EnumGui.GUI_TCU_PLAYER_TARGETS, gui.getTurret().getEntityId(), 0, 0);
            return true;
        } else if( button == pageUpgrades ) {
            TurretModRebirth.proxy.openGui(mc.thePlayer, EnumGui.GUI_TCU_UPGRADES, gui.getTurret().getEntityId(), 0, 0);
            return true;
        }
        return false;
    }
}
