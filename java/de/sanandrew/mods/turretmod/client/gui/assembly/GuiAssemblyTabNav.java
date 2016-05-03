/**
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.gui.assembly;

import de.sanandrew.mods.turretmod.util.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiAssemblyTabNav
        extends GuiButton
{
    private boolean isDown;

	public GuiAssemblyTabNav(int id, int posX, int posY, boolean isDown) {
		super(id, posX, posY, "");
		this.width = 16;
		this.height = 9;
		this.isDown = isDown;
	}

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if( this.visible && this.enabled ) {
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            mc.renderEngine.bindTexture(Textures.GUI_ASSEMBLY_CRF.getResource());
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int hoverState = this.getHoverState(this.field_146123_n);

            this.drawTexturedModalRect(this.xPosition, this.yPosition, 50 + (this.isDown ? 16 : 0), 227 + 9 * hoverState, this.width, this.height);

            this.mouseDragged(mc, mouseX, mouseY);

            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}
