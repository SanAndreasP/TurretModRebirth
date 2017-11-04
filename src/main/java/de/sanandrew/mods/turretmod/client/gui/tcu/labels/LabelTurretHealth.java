/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.client.gui.tcu.labels;

import de.sanandrew.mods.sanlib.lib.ColorObj;
import de.sanandrew.mods.turretmod.api.client.tcu.ILabelElement;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.util.Lang;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LabelTurretHealth
        implements ILabelElement
{
    @Override
    public boolean showElement(ITurretInst turretInst) {
        return true;
    }

    @Override
    public float getHeight(ITurretInst turretInst, FontRenderer stdFontRenderer) {
        return stdFontRenderer.FONT_HEIGHT + 6.0F;
    }

    @Override
    public float getWidth(ITurretInst turretInst, FontRenderer stdFontRenderer) {
        return stdFontRenderer.getStringWidth(getLabel(turretInst));
    }

    @Override
    public void doRenderQuads(ITurretInst turretInst, float maxWidth, float progress, FontRenderer stdFontRenderer, float currHeight, VertexBuffer tessBuffer) {
        float healthPerc = turretInst.getEntity().getHealth() / turretInst.getEntity().getMaxHealth() * maxWidth;
        currHeight += stdFontRenderer.FONT_HEIGHT + 2.0F;
        addQuad(tessBuffer, 0.0D,       currHeight, healthPerc, currHeight + 2.0D, new ColorObj(1.0F, 0.3F, 0.3F, Math.max(progress, 4.0F / 255.0F)));
        addQuad(tessBuffer, healthPerc, currHeight, maxWidth,   currHeight + 2.0D, new ColorObj(0.4F, 0.1F, 0.1F, Math.max(progress, 4.0F / 255.0F)));
    }

    @Override
    public void doRenderTextured(ITurretInst turretInst, float maxWidth, float progress, FontRenderer stdFontRenderer) {
        stdFontRenderer.drawString(getLabel(turretInst), 0.0F, 0.0F, new ColorObj(1.0F, 0.3F, 0.3F, Math.max(progress, 4.0F / 255.0F)).getColorInt(), false);
    }

    private static String getLabel(ITurretInst turretInst) {
        return Lang.translate(Lang.TCU_LABEL_HEALTH, String.format("%.2f/%.2f", turretInst.getEntity().getHealth(), turretInst.getEntity().getMaxHealth()));
    }

    private static void addQuad(VertexBuffer buf, double minX, double minY, double maxX, double maxY, ColorObj clr) {
        buf.pos(minX, minY, 0.0D).color(clr.fRed(), clr.fGreen(), clr.fBlue(), clr.fAlpha()).endVertex();
        buf.pos(minX, maxY, 0.0D).color(clr.fRed(), clr.fGreen(), clr.fBlue(), clr.fAlpha()).endVertex();
        buf.pos(maxX, maxY, 0.0D).color(clr.fRed(), clr.fGreen(), clr.fBlue(), clr.fAlpha()).endVertex();
        buf.pos(maxX, minY, 0.0D).color(clr.fRed(), clr.fGreen(), clr.fBlue(), clr.fAlpha()).endVertex();
    }
}