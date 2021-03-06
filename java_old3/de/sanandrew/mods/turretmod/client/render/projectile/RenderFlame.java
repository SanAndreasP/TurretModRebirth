/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.render.projectile;

import de.sanandrew.mods.turretmod.api.client.render.IRender;
import de.sanandrew.mods.turretmod.api.client.render.IRenderInst;
import de.sanandrew.mods.turretmod.client.init.ClientProxy;
import de.sanandrew.mods.turretmod.registry.Resources;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderFlame<T extends Entity>
        implements IRender<T>
{
    private final boolean purify;

    RenderFlame(boolean purify) {
        this.purify = purify;
    }

    @Override
    public void doRender(IRenderInst<T> render, T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        RenderManager renderManager = render.getRender().getRenderManager();

        GlStateManager.pushMatrix();
        render.bindRenderEntityTexture(entity);
        GlStateManager.translate((float)x, (float)y, (float)z);
        double scale = 2.0F;// * (entity.deathUpdateTicks / 20.0F);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.disableLighting();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float[] prevBright = ClientProxy.forceGlow();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.rotate(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((renderManager.options.thirdPersonView == 2 ? -1 : 1) * -renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        buf.begin(7, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-0.125D, -0.0625D, 0.0D).tex(0.0D, 1.0D).endVertex();
        buf.pos(0.125D, -0.0625D, 0.0D).tex(1.0D, 1.0D).endVertex();
        buf.pos(0.125D, 0.1875D, 0.0D).tex(1.0D, 0.0D).endVertex();
        buf.pos(-0.125D, 0.1875D, 0.0D).tex(0.0D, 0.0D).endVertex();
        tess.draw();

        ClientProxy.resetGlow(prevBright);

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public ResourceLocation getRenderTexture(T entity) {
        return this.purify ? Resources.PROJECTILE_FLAME_BLUE.resource : Resources.PROJECTILE_FLAME_RED.resource;
    }
}
