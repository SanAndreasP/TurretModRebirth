/**
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.gui.tinfo.entry;

import de.sanandrew.mods.turretmod.client.gui.tinfo.GuiTurretInfo;
import de.sanandrew.mods.turretmod.client.util.TmrClientUtils;
import de.sanandrew.mods.turretmod.util.Lang;
import de.sanandrew.mods.turretmod.util.Resources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public abstract class TurretInfoEntry
{
    public static final int MAX_ENTRY_WIDTH = 168;
    public static final int MAX_ENTRY_HEIGHT = 183;
    private ItemStack icon;
    private String title;

    protected final String txtRounds;
    protected final String txtDps;
    protected final String txtHealth;
    protected final String txtHealthVal;
    protected final String txtTurret;
    protected final String txtCrft;
    protected final String txtWorkbench;
    protected final String txtPrereq;
    protected final String txtRange;
    protected final String txtAmmoCap;
    protected final String txtAmmoUse;

//    protected static final RenderItem ITEM_RENDER = new RenderItem();

    protected TurretInfoEntry(ItemStack icon, String title) {
        this.icon = icon;
        this.title = title;

        this.txtRounds = Lang.translate(Lang.TINFO_ENTRY_ROUNDS);
        this.txtDps = Lang.translate(Lang.TINFO_ENTRY_DPS);
        this.txtHealth = Lang.translate(Lang.TINFO_ENTRY_HEALTH);
        this.txtHealthVal = Lang.translate(Lang.TINFO_ENTRY_HEALTHVAL);
        this.txtTurret = Lang.translate(Lang.TINFO_ENTRY_TURRET);
        this.txtCrft = Lang.translate(Lang.TINFO_ENTRY_CRAFTING);
        this.txtWorkbench = Lang.translate(Lang.TINFO_ENTRY_WORKBENCH);
        this.txtPrereq = Lang.translate(Lang.TINFO_ENTRY_PREREQ);
        this.txtRange = Lang.translate(Lang.TINFO_ENTRY_RANGE);
        this.txtAmmoCap = Lang.translate(Lang.TINFO_ENTRY_AMMOCAP);
        this.txtAmmoUse = Lang.translate(Lang.TINFO_ENTRY_AMMOUSE);
    }

    public final ItemStack getIcon() {
        return this.icon.copy();
    }

    public final String getTitle() {
        return this.title;
    }

    protected static void drawItem(Minecraft mc, int x, int y, ItemStack stack, float scale) {
        GL11.glPushMatrix();

        GL11.glTranslatef(x, y, 0.0F);
        GL11.glScalef(scale, scale, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        //TODO: enable item renderer
//        ITEM_RENDER.zLevel = -50.0F;
//        ITEM_RENDER.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), stack, 0, 0);
//        ITEM_RENDER.renderItemOverlayIntoGUI(mc.fontRenderer, mc.getTextureManager(), stack, 0, 0);
//        ITEM_RENDER.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        GL11.glPopMatrix();
    }

    protected static void drawMiniItem(GuiTurretInfo gui, int x, int y, int mouseX, int mouseY, int scrollY, ItemStack stack, boolean drawTooltip) {
        gui.mc.getTextureManager().bindTexture(Resources.GUI_TURRETINFO.getResource());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glScalef(0.5F, 0.5F, 1.0F);
        gui.drawTexturedModalRect(0, 0, 192, 0, 18, 18);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        boolean mouseOver = mouseY >= 0 && mouseY < MAX_ENTRY_HEIGHT && mouseX >= x && mouseX < x + 9 && mouseY >= y - scrollY && mouseY < y + 9 - scrollY;
        if( mouseOver ) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, MAX_ENTRY_HEIGHT - 20 + scrollY, 32.0F);
            Gui.drawRect(0, 0, MAX_ENTRY_WIDTH, 20, 0xD0000000);

            List tooltip = TmrClientUtils.getTooltipWithoutShift(stack);
            gui.mc.fontRendererObj.drawString(tooltip.get(0).toString(), 22, 2, 0xFFFFFFFF, false);
            if( drawTooltip && tooltip.size() > 1 ) {
                gui.mc.fontRendererObj.drawString(tooltip.get(1).toString(), 22, 11, 0xFF808080, false);
            }

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();
            //TODO: enable item renderer
//            ITEM_RENDER.zLevel = -50.0F;
//            ITEM_RENDER.renderItemAndEffectIntoGUI(gui.mc.fontRenderer, gui.mc.getTextureManager(), stack, 2, 2);
//            ITEM_RENDER.renderItemOverlayIntoGUI(gui.mc.fontRenderer, gui.mc.getTextureManager(), stack, 2, 2);
//            ITEM_RENDER.zLevel = 0.0F;
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);

            GL11.glPopMatrix();
        }

        GL11.glTranslatef(x, y, 32.0F);

        GL11.glScalef(0.5F, 0.5F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        //TODO: enable item renderer
//        ITEM_RENDER.zLevel = -50.0F;
//        ITEM_RENDER.renderItemAndEffectIntoGUI(gui.mc.fontRenderer, gui.mc.getTextureManager(), stack, 1, 1);
//        ITEM_RENDER.renderItemOverlayIntoGUI(gui.mc.fontRenderer, gui.mc.getTextureManager(), stack, 1, 1);
//        ITEM_RENDER.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        if( mouseOver ) {
            GL11.glTranslatef(0, 0, 32.0F);
            Gui.drawRect(1, 1, 17, 17, 0x80FFFFFF);
        }

        GL11.glPopMatrix();
    }

    public abstract void drawPage(GuiTurretInfo gui, int mouseX, int mouseY, int scrollY, float partTicks);

    public abstract int getPageHeight();

    public boolean actionPerformed(GuiButton btn) {
        return false;
    }

    public void initEntry(GuiTurretInfo gui) { }
}