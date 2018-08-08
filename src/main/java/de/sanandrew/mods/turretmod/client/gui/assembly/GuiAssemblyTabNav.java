/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.gui.assembly;

import de.sanandrew.mods.turretmod.util.Resources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class GuiAssemblyTabNav
        extends GuiButton
{
    private final boolean isDown;

	GuiAssemblyTabNav(int id, int posX, int posY, boolean isDown) {
		super(id, posX, posY, "");
		this.width = 16;
		this.height = 9;
		this.isDown = isDown;
	}

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partTicks) {
        if( this.visible && this.enabled ) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            mc.renderEngine.bindTexture(Resources.GUI_ASSEMBLY_CRF.resource);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int hoverState = this.getHoverState(this.hovered);

            this.drawTexturedModalRect(this.x, this.y, 50 + (this.isDown ? 16 : 0), 227 + 9 * hoverState, this.width, this.height);

            this.mouseDragged(mc, mouseX, mouseY);

            GlStateManager.disableBlend();
        }
    }
}
