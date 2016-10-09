/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.render.turret;

import de.sanandrew.mods.turretmod.entity.turret.EntityTurret;
import de.sanandrew.mods.turretmod.util.TurretModRebirth;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.InvocationTargetException;

public class RenderTurret
        extends RenderLiving<EntityTurret>
{
    public RenderTurret(RenderManager manager, ModelBase standardModel) {
        super(manager, standardModel, 0.5F);

        try {
            this.addLayer(new LayerTurretGlow<>(this, standardModel.getClass().getConstructor(float.class).newInstance(0.001F)));
        } catch( NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex ) {
            TurretModRebirth.LOG.log(Level.ERROR, "Could not instanciate model class! Make sure it has a constructor with a one float parameter (scale)! Glowmap disabled.", ex);
        }

        this.addLayer(new LayerTurretUpgrades<>());
    }

    @Override
    public void doRender(EntityTurret entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        renderTurretRange(entity, x, y, z);
    }

    @Override
    protected void renderModel(EntityTurret turret, float limbSwing, float limbSwingAmount, float rotFloat, float rotYaw, float rotPitch, float scale) {
        super.renderModel(turret, limbSwing, limbSwingAmount, rotFloat, rotYaw, rotPitch, scale);
    }

    @Override
    protected void rotateCorpse(EntityTurret turret, float x, float y, float z) {
        super.rotateCorpse(turret, x, y, z);

        if( turret.isUpsideDown ) {
            GlStateManager.translate(0.0F, turret.height + 0.2F, 0.0F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTurret entity) {
        return entity.getStandardTexture();
    }

    @Override
    protected boolean canRenderName(EntityTurret entity) {
        return false;
    }

    private static void renderTurretRange(EntityTurret turret, double x, double y, double z) {
        if( turret.showRange ) {
            GlStateManager.disableTexture2D();

            float prevBrightX = OpenGlHelper.lastBrightnessX;
            float prevBrightY = OpenGlHelper.lastBrightnessY;
            int brightness = 0xF0;
            int brightX = brightness % 65536;
            int brightY = brightness / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightX, brightY);

            Tessellator tess = Tessellator.getInstance();
            VertexBuffer buf = tess.getBuffer();

            AxisAlignedBB aabb = turret.getTargetProcessor().getRangeBB().offset(-turret.posX, -turret.posY, -turret.posZ);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.glLineWidth(5.0F);
            buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(0, aabb.minY, 0).color(0, 0, 0, 255).endVertex();
            buf.pos(0, aabb.maxY, 0).color(0, 0, 0, 255).endVertex();
            tess.draw();
            GlStateManager.glLineWidth(3.0F);
            buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(0, aabb.minY, 0).color(255, 255, 255, 255).endVertex();
            buf.pos(0, aabb.maxY, 0).color(255, 255, 255, 255).endVertex();
            tess.draw();

            GlStateManager.glLineWidth(3.0F);
            for( double cx = aabb.minX; cx <= aabb.maxX; cx += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.pos(cx, aabb.minY, aabb.minZ).color(0, 0, 0, 255).endVertex();
                buf.pos(cx, aabb.minY, aabb.maxZ).color(0, 0, 0, 255).endVertex();
                buf.pos(cx, aabb.maxY, aabb.maxZ).color(0, 0, 0, 255).endVertex();
                buf.pos(cx, aabb.maxY, aabb.minZ).color(0, 0, 0, 255).endVertex();
                tess.draw();
            }
            for( double cy = aabb.minY; cy <= aabb.maxY; cy += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.pos(aabb.minX, cy, aabb.minZ).color(0, 0, 0, 255).endVertex();
                buf.pos(aabb.minX, cy, aabb.maxZ).color(0, 0, 0, 255).endVertex();
                buf.pos(aabb.maxX, cy, aabb.maxZ).color(0, 0, 0, 255).endVertex();
                buf.pos(aabb.maxX, cy, aabb.minZ).color(0, 0, 0, 255).endVertex();
                tess.draw();
            }
            for( double cz = aabb.minZ; cz <= aabb.maxZ; cz += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.pos(aabb.minX, aabb.minY, cz).color(0, 0, 0, 255).endVertex();
                buf.pos(aabb.minX, aabb.maxY, cz).color(0, 0, 0, 255).endVertex();
                buf.pos(aabb.maxX, aabb.maxY, cz).color(0, 0, 0, 255).endVertex();
                buf.pos(aabb.maxX, aabb.minY, cz).color(0, 0, 0, 255).endVertex();
                tess.draw();
            }
            GlStateManager.glLineWidth(0.1F);
            for( double cx = aabb.minX; cx <= aabb.maxX; cx += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.pos(cx, aabb.minY, aabb.minZ).color(255, 255, 255, 255).endVertex();
                buf.pos(cx, aabb.minY, aabb.maxZ).color(255, 255, 255, 255).endVertex();
                buf.pos(cx, aabb.maxY, aabb.maxZ).color(255, 255, 255, 255).endVertex();
                buf.pos(cx, aabb.maxY, aabb.minZ).color(255, 255, 255, 255).endVertex();
                tess.draw();
            }
            for( double cy = aabb.minY; cy <= aabb.maxY; cy += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.pos(aabb.minX, cy, aabb.minZ).color(255, 255, 255, 255).endVertex();
                buf.pos(aabb.minX, cy, aabb.maxZ).color(255, 255, 255, 255).endVertex();
                buf.pos(aabb.maxX, cy, aabb.maxZ).color(255, 255, 255, 255).endVertex();
                buf.pos(aabb.maxX, cy, aabb.minZ).color(255, 255, 255, 255).endVertex();
                tess.draw();
            }
            for( double cz = aabb.minZ; cz <= aabb.maxZ; cz += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.pos(aabb.minX, aabb.minY, cz).color(255, 255, 255, 255).endVertex();
                buf.pos(aabb.minX, aabb.maxY, cz).color(255, 255, 255, 255).endVertex();
                buf.pos(aabb.maxX, aabb.maxY, cz).color(255, 255, 255, 255).endVertex();
                buf.pos(aabb.maxX, aabb.minY, cz).color(255, 255, 255, 255).endVertex();
                tess.draw();
            }
            GlStateManager.popMatrix();

            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevBrightX, prevBrightY);

            GlStateManager.enableTexture2D();
        }
    }
}
