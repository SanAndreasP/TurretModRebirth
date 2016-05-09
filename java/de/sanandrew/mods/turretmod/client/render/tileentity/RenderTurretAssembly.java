/**
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.render.tileentity;

import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.client.model.block.ModelTurretAssembly;
import de.sanandrew.mods.turretmod.tileentity.TileEntityTurretAssembly;
import de.sanandrew.mods.turretmod.util.Textures;
import net.darkhax.bookshelf.lib.util.ItemStackUtils;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;

public class RenderTurretAssembly
        extends TileEntitySpecialRenderer
{
    private ModelTurretAssembly modelBlock = new ModelTurretAssembly();

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partTicks) {
        TileEntityTurretAssembly te = ((TileEntityTurretAssembly) tile);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);

        this.bindTexture(Textures.TILE_TURRET_ASSEMBLY.getResource());
        this.modelBlock.render(0.0625F, partTicks, te);

        renderItem(te);

        GL11.glPopMatrix();
    }

    private static void renderItem(TileEntityTurretAssembly assembly) {
        ContextCapabilities glCapabilities = GLContext.getCapabilities();

        ItemStack crfStack = assembly.currCrafting != null ? assembly.currCrafting.getValue1() : assembly.getStackInSlot(0);
        int meta = assembly.hasWorldObj() ? BlockRegistry.turretAssembly.getDirection(assembly.getBlockMetadata()) - 2 : 0;

        GL11.glPushMatrix();
        GL11.glRotatef(90.0F * meta, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(0.0F, 0.795F, -0.2125F);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(0.75F, 0.75F, 1.0F);

        RenderItem.renderInFrame = true;

        if( ItemStackUtils.isValidStack(crfStack) ) {
            EntityItem entityitem = new EntityItem(assembly.getWorldObj(), 0.0D, 0.0D, 0.0D, crfStack.copy());
            entityitem.getEntityItem().stackSize = 1;
            entityitem.hoverStart = 0.0F;

            float scale = Math.max(0.0F, (assembly.ticksCrafted - 15.0F) / (assembly.maxTicksCrafted - 15.0F));

            if( glCapabilities.OpenGL14 && glCapabilities.GL_EXT_blend_color ) {
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                GL14.glBlendColor(1.0f, 1.0f, 1.0f, scale);
                RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
                GL11.glDisable(GL11.GL_BLEND);
            } else {
                RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
            }
        }

        if( assembly.hasAutoUpgrade() ) {
            EntityItem entityitem = new EntityItem(assembly.getWorldObj(), 0.0D, 0.0D, 0.0D, assembly.getStackInSlot(1).copy());
            entityitem.getEntityItem().stackSize = 1;
            entityitem.hoverStart = 0.0F;

            GL11.glPushMatrix();
            GL11.glScalef(1.0F / 0.75F, 1.0F / 0.75F, 1.0F);
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.05F, -0.2F, -0.35F);
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
            GL11.glPopMatrix();
        }

        if( assembly.hasSpeedUpgrade() ) {
            EntityItem entityitem = new EntityItem(assembly.getWorldObj(), 0.0D, 0.0D, 0.0D, assembly.getStackInSlot(2).copy());
            entityitem.getEntityItem().stackSize = 1;
            entityitem.hoverStart = 0.0F;

            GL11.glPushMatrix();
            GL11.glScalef(1.0F / 0.75F, 1.0F / 0.75F, 1.0F);
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.05F, -0.2F, -0.4F);
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
            GL11.glPopMatrix();
        }

        if( assembly.hasFilterUpgrade() ) {
            EntityItem entityitem = new EntityItem(assembly.getWorldObj(), 0.0D, 0.0D, 0.0D, assembly.getStackInSlot(3).copy());
            entityitem.getEntityItem().stackSize = 1;
            entityitem.hoverStart = 0.0F;

            GL11.glPushMatrix();
            GL11.glScalef(1.0F / 0.75F, 1.0F / 0.75F, 1.0F);
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.05F, -0.2F, -0.45F);
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
            GL11.glPopMatrix();
        }

        RenderItem.renderInFrame = false;
        GL11.glPopMatrix();
    }
}
