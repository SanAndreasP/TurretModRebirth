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
import de.sanandrew.mods.turretmod.registry.Lang;
import de.sanandrew.mods.turretmod.registry.turret.forcefield.TurretForcefield;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;

public class LabelTurretTarget
        implements ILabelElement
{
    @Override
    public boolean showElement(ITurretInst turretInst) {
        return !(turretInst.getTurret() instanceof TurretForcefield);
    }

    @Override
    public int getPriority() {
        return -100;
    }

    @Override
    public float getHeight(ITurretInst turretInst, FontRenderer fontRenderer) {
        return fontRenderer.FONT_HEIGHT + 2.0F;
    }

    @Override
    public float getWidth(ITurretInst turretInst, FontRenderer stdFontRenderer) {
        return stdFontRenderer.getStringWidth(getLabel(turretInst));
    }

    @Override
    public void renderTextured(ITurretInst turretInst, float maxWidth, float progress, FontRenderer fontRenderer) {
        int color = new ColorObj(1.0F, 1.0F, 1.0F, Math.max(progress, 0x4 / 255.0F)).getColorInt();
        fontRenderer.drawString(getLabel(turretInst), 0.0F, 0.0F, color, false);
    }

    private static String getLabel(ITurretInst turretInst) {
        Entity target = turretInst.getTargetProcessor().getTarget();
        return LangUtils.translate(Lang.TCU_LABEL_TARGET, target == null ? "n/a" : target.getName());
    }
}
