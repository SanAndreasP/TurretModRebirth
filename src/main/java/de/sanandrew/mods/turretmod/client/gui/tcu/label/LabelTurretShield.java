/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.client.gui.tcu.label;

import de.sanandrew.mods.sanlib.lib.ColorObj;
import de.sanandrew.mods.sanlib.lib.util.LangUtils;
import de.sanandrew.mods.turretmod.api.client.tcu.ILabelElement;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.client.init.ClientProxy;
import de.sanandrew.mods.turretmod.registry.Lang;
import de.sanandrew.mods.turretmod.registry.turret.shieldgen.ShieldTurret;
import de.sanandrew.mods.turretmod.registry.turret.shieldgen.TurretForcefield;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;

public class LabelTurretShield
        implements ILabelElement
{
    @Override
    public boolean showElement(ITurretInst turretInst) {
        return turretInst.getTurret() instanceof TurretForcefield;
    }

    @Override
    public float getHeight(ITurretInst turretInst, FontRenderer fontRenderer) {
        return fontRenderer.FONT_HEIGHT + 6.0F;
    }

    @Override
    public float getWidth(ITurretInst turretInst, FontRenderer stdFontRenderer) {
        return stdFontRenderer.getStringWidth(getLabel(turretInst));
    }

    @Override
    public void renderQuads(ITurretInst turretInst, float maxWidth, float progress, FontRenderer fontRenderer, float currHeight, BufferBuilder buffer) {
        ShieldTurret shield = turretInst.getRAM(null);
        float shieldPerc = shield == null ? 0.0F : shield.getValue() / shield.getMaxValue() * maxWidth;

        currHeight += fontRenderer.FONT_HEIGHT + 2.0F;

        addQuad(buffer, 0.0D, currHeight, shieldPerc, currHeight + 2.0D, new ColorObj(1.0F, 1.0F, 0.0F, Math.max(progress, 4.0F / 255.0F)));
        addQuad(buffer, shieldPerc, currHeight, maxWidth, currHeight + 2.0D, new ColorObj(0.4F, 0.4F, 0.0F, Math.max(progress, 4.0F / 255.0F)));
    }

    @Override
    public void renderTextured(ITurretInst turretInst, float maxWidth, float progress, FontRenderer fontRenderer) {
        fontRenderer.drawString(getLabel(turretInst), 0.0F, 0.0F, new ColorObj(1.0F, 1.0F, 0.0F, Math.max(progress, 4.0F / 255.0F)).getColorInt(), false);
    }

    private static String getLabel(ITurretInst turretInst) {
        ShieldTurret shield = turretInst.getRAM(null);

        if( shield != null ) {
            if( shield.isInRecovery() ) {
                return LangUtils.translate(Lang.TCU_LABEL_TURRETSHIELD_RECV, String.format("%.0f %%", shield.getRecovery() * 100.0F));
            } else {
                return LangUtils.translate(Lang.TCU_LABEL_TURRETSHIELD, String.format("%.2f/%.2f", shield.getValue(), shield.getMaxValue()));
            }
        } else {
            return "";
        }
    }

    private static void addQuad(BufferBuilder buf, double minX, double minY, double maxX, double maxY, ColorObj clr) {
        ClientProxy.addQuad(buf, minX, minY, maxX, maxY, clr, clr);
    }
}
