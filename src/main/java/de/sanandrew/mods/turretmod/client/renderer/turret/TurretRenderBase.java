/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.client.renderer.turret;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.sanandrew.mods.turretmod.api.turret.ITurretEntity;
import de.sanandrew.mods.turretmod.client.renderer.turret.layer.LayerTurretGlow;
import de.sanandrew.mods.turretmod.client.renderer.turret.layer.LayerTurretUpgrades;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class TurretRenderBase<T extends LivingEntity & ITurretEntity, M extends EntityModel<T>>
        extends LivingRenderer<T, M>
{
    public TurretRenderBase(EntityRendererManager manager, Supplier<M> modelFactory) {
        super(manager, modelFactory.get(), 0.5F);

        this.addTurretLayers();
    }

    protected void addTurretLayers() {
        this.addLayer(new LayerTurretUpgrades<>(this));
        this.addLayer(new LayerTurretGlow<>(this));
    }

    @Override
    public void render(@Nonnull T turretInst, float entityYaw, float partialTicks, @Nonnull MatrixStack stack,
                       @Nonnull IRenderTypeBuffer buffer, int packedLight)
    {
        super.render(turretInst, entityYaw, partialTicks, stack, buffer, packedLight);

        renderTurretRange(turretInst, stack);
    }

    @Override
    protected boolean shouldShowName(@Nonnull T entity) {
        return false;
    }

    /**
     * This is returning the glow layer texture for {@link LayerTurretGlow}, since the {@link TurretRenderer} handles the regular model rendering
     */
    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull T entity) {
        return entity.getDelegate().getBaseTexture(entity);
    }

    protected static void renderTurretRange(ITurretEntity turret, MatrixStack stack) {
        if( turret.shouldShowRange() ) {
            RenderSystem.disableTexture();

            Tessellator   tess = Tessellator.getInstance();
            BufferBuilder buf  = tess.getBuilder();

            AxisAlignedBB aabb = turret.getTargetProcessor().getAdjustedRange(false);

            stack.pushPose();
            GlStateManager._lineWidth(5.0F);
            buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            buf.vertex(0, aabb.minY, 0).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
            buf.vertex(0, aabb.maxY, 0).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
            tess.end();
            GlStateManager._lineWidth(3.0F);
            buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            buf.vertex(0, aabb.minY, 0).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
            buf.vertex(0, aabb.maxY, 0).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
            tess.end();

            GlStateManager._lineWidth(3.0F);
            for( double cx = aabb.minX; cx <= aabb.maxX; cx += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.vertex(cx, aabb.minY, aabb.minZ).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(cx, aabb.minY, aabb.maxZ).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(cx, aabb.maxY, aabb.maxZ).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(cx, aabb.maxY, aabb.minZ).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                tess.end();
            }
            for( double cy = aabb.minY; cy <= aabb.maxY; cy += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.vertex(aabb.minX, cy, aabb.minZ).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.minX, cy, aabb.maxZ).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.maxX, cy, aabb.maxZ).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.maxX, cy, aabb.minZ).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                tess.end();
            }
            for( double cz = aabb.minZ; cz <= aabb.maxZ; cz += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.vertex(aabb.minX, aabb.minY, cz).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.minX, aabb.maxY, cz).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.maxX, aabb.maxY, cz).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.maxX, aabb.minY, cz).color(0, 0, 0, 255).uv2(0, 0xF0).endVertex();
                tess.end();
            }
            GlStateManager._lineWidth(0.1F);
            for( double cx = aabb.minX; cx <= aabb.maxX; cx += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.vertex(cx, aabb.minY, aabb.minZ).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(cx, aabb.minY, aabb.maxZ).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(cx, aabb.maxY, aabb.maxZ).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(cx, aabb.maxY, aabb.minZ).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                tess.end();
            }
            for( double cy = aabb.minY; cy <= aabb.maxY; cy += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.vertex(aabb.minX, cy, aabb.minZ).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.minX, cy, aabb.maxZ).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.maxX, cy, aabb.maxZ).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.maxX, cy, aabb.minZ).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                tess.end();
            }
            for( double cz = aabb.minZ; cz <= aabb.maxZ; cz += 0.5F ) {
                buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                buf.vertex(aabb.minX, aabb.minY, cz).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.minX, aabb.maxY, cz).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.maxX, aabb.maxY, cz).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                buf.vertex(aabb.maxX, aabb.minY, cz).color(255, 255, 255, 255).uv2(0, 0xF0).endVertex();
                tess.end();
            }
            stack.popPose();

            RenderSystem.enableTexture();
        }
    }
}
