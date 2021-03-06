/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.render.tileentity;

import de.sanandrew.mods.sanlib.lib.Tuple;
import de.sanandrew.mods.sanlib.lib.client.ShaderHelper;
import de.sanandrew.mods.sanlib.lib.client.util.RenderUtils;
import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.client.init.ClientProxy;
import de.sanandrew.mods.turretmod.client.model.block.ModelTurretAssembly;
import de.sanandrew.mods.turretmod.client.shader.ShaderItemAlphaOverride;
import de.sanandrew.mods.turretmod.client.shader.Shaders;
import de.sanandrew.mods.turretmod.inventory.AssemblyInventory;
import de.sanandrew.mods.turretmod.registry.Resources;
import de.sanandrew.mods.turretmod.tileentity.assembly.TileEntityTurretAssembly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderTurretAssembly
        extends TileEntitySpecialRenderer<TileEntityTurretAssembly>
{
    private final ModelTurretAssembly modelBlock = new ModelTurretAssembly();

    private float armX;
    private float armZ;

    private final ShaderItemAlphaOverride shaderCallback = new ShaderItemAlphaOverride();

    @Override
    public void render(TileEntityTurretAssembly tile, double x, double y, double z, float partTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);

        armX = Math.max(2.0F, Math.min(12.0F, tile.prevRobotArmX + (tile.robotArmX - tile.prevRobotArmX) * partTicks)) - 7.0F;
        armZ = Math.max(-11.0F, Math.min(-3.0F, tile.prevRobotArmY + (tile.robotArmY - tile.prevRobotArmY) * partTicks));

        this.bindTexture(Resources.TILE_TURRET_ASSEMBLY.resource);
        this.modelBlock.render(0.0625F, tile, armX, armZ);

        renderItem(tile);

        if( tile.isActiveClient && tile.robotArmX >= 4.0F && tile.robotArmX <= 10.0F && tile.robotArmY <= -3.5F && tile.robotArmY >= -9.5F ) {
            tile.spawnParticle = this.renderLaser(BlockRegistry.TURRET_ASSEMBLY.getDirection(tile.getBlockMetadata()), tile.getPos());
        }

        GlStateManager.popMatrix();
    }

    private Tuple renderLaser(EnumFacing facing, BlockPos pos) {
        float laserX = ((this.armX) / 16.0F);
        float laserZ = ((this.armZ + 5.5F) / 16.0F);

        float lx;
        switch( facing ) {
            case WEST:
                lx = laserX;
                laserX = laserZ;
                laserZ = -lx;
                break;
            case NORTH:
                laserX = -laserX;
                laserZ = -laserZ;
                break;
            case EAST:
                lx = laserX;
                laserX = -laserZ;
                laserZ = lx;
                break;
        }

        int tileX = pos.getX();
        int tileY = pos.getY();
        int tileZ = pos.getZ();
        float dist = (float) Minecraft.getMinecraft().player.getDistance(tileX + 0.5F, tileY + 0.5F, tileZ + 0.5F);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.translate(laserX, 0.5F, laserZ);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        float[] prevBright = ClientProxy.forceGlow();

        GlStateManager.glLineWidth(Math.min(20.0F, 20.0F / (dist)));
        buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(0.0F, 0.1F, 0.0F).color(255, 0, 0, 64).endVertex();
        buf.pos(0.0F, 1.0F, 0.0F).color(255, 0, 0, 64).endVertex();
        tess.draw();

        GlStateManager.glLineWidth(Math.min(5.0F, 5.0F / (dist)));
        buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(0.0F, 0.1F, 0.0F).color(255, 0, 0, 128).endVertex();
        buf.pos(0.0F, 1.0F, 0.0F).color(255, 0, 0, 128).endVertex();
        tess.draw();

        ClientProxy.resetGlow(prevBright);

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        return new Tuple((double) (tileX + 0.50F + laserX), (double) (tileY + 0.65F), (double) (tileZ + 0.50F - laserZ));
    }

    private void renderItem(TileEntityTurretAssembly assembly) {
        int xShift = 0;
        ItemStack crfStack = assembly.currRecipe != null ? assembly.currRecipe.getRecipeOutput() : assembly.getInventory().getStackInSlot(0);

        GlStateManager.pushMatrix();
        GlStateManager.rotate((float)(90.0D * BlockRegistry.TURRET_ASSEMBLY.getDirection(assembly.getBlockMetadata()).getHorizontalIndex()), 0.0F, 1.0F, 0.0F);

        if( ItemStackUtils.isValid(crfStack) ) {
            World world = assembly.getWorld();
            BlockPos pos = assembly.getPos();

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.shaderCallback.alphaMulti = Math.max(0.0F, (assembly.getTicksCrafted() - 15.0F) / (assembly.getMaxTicksCrafted() - 15.0F));
            this.shaderCallback.brightness = world.getCombinedLight(pos, 0);
            ShaderHelper.useShader(Shaders.alphaOverride, this.shaderCallback::call);
            RenderUtils.renderStackInWorld(crfStack, 0.0D, 0.802D, 0.0D, -90.0F, 180.0F, 0.0F, 0.35D);
            ShaderHelper.releaseShader();
            GlStateManager.disableBlend();
        }

        if( assembly.hasAutoUpgrade() ) {
            RenderUtils.renderStackInWorld(assembly.getInventory().getStackInSlot(AssemblyInventory.SLOT_UPGRADE_AUTO), -0.325D + xShift++ * 0.215D, 0.84D, -0.40D, 0.0F, 0.0F, 0.0F, 0.15D);
        }
        if( assembly.hasSpeedUpgrade() ) {
            RenderUtils.renderStackInWorld(assembly.getInventory().getStackInSlot(AssemblyInventory.SLOT_UPGRADE_SPEED), -0.325D + xShift++ * 0.215D, 0.84D, -0.40D, 0.0F, 0.0F, 0.0F, 0.15D);
        }
        if( assembly.hasFilterUpgrade() ) {
            RenderUtils.renderStackInWorld(assembly.getInventory().getStackInSlot(AssemblyInventory.SLOT_UPGRADE_FILTER), -0.325D + xShift++ * 0.215D, 0.84D, -0.40D, 0.0F, 0.0F, 0.0F, 0.15D);
        }
        if( assembly.hasRedstoneUpgrade() ) {
            RenderUtils.renderStackInWorld(assembly.getInventory().getStackInSlot(AssemblyInventory.SLOT_UPGRADE_REDSTONE), -0.325D + xShift * 0.215D, 0.84D, -0.40D, 0.0F, 0.0F, 0.0F, 0.15D);
        }
        GlStateManager.popMatrix();
    }
}
