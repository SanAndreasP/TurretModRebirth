package de.sanandrew.mods.turretmod.client.renderer.turret;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.sanandrew.mods.sanlib.lib.ColorObj;
import de.sanandrew.mods.turretmod.api.Resources;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.client.tcu.ILabelRegistry;
import de.sanandrew.mods.turretmod.api.client.tcu.ILabelRenderer;
import de.sanandrew.mods.turretmod.api.turret.ITurretEntity;
import de.sanandrew.mods.turretmod.client.renderer.turret.label.Ammo;
import de.sanandrew.mods.turretmod.client.renderer.turret.label.Health;
import de.sanandrew.mods.turretmod.client.renderer.turret.label.Name;
import de.sanandrew.mods.turretmod.client.renderer.turret.label.Target;
import de.sanandrew.mods.turretmod.entity.turret.TurretEntity;
import de.sanandrew.mods.turretmod.item.TurretControlUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.model.TransformationHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

@OnlyIn(Dist.CLIENT)
public final class LabelRegistry
        implements ILabelRegistry
{
    public static final LabelRegistry INSTANCE = new LabelRegistry();

    private static final Map<ResourceLocation, ILabelRenderer> LABEL_RENDERERS     = new HashMap<>();
    private static final List<ILabelRenderer>                  LABEL_RENDERER_LIST = new ArrayList<>();
    private static final List<ILabelRenderer>                  LABEL_RENDERER_VIEW = Collections.unmodifiableList(LABEL_RENDERER_LIST);

    private static final ILabelRenderer EMPTY = new ILabelRenderer() {
        @Override public boolean isVisible(ITurretEntity turret) { return false; }
        @Override public int getMinWidth(ILabelRegistry registry, ITurretEntity turret) { return 0; }
        @Override public int getHeight(ILabelRegistry registry, ITurretEntity turret) { return 0; }
        @Nonnull @Override public ResourceLocation getId() { return Resources.NULL; }
    };

    private final List<Label> currentLabels = new ArrayList<>();

    private LabelRegistry() { }

    @Override
    public void register(@Nonnull ILabelRenderer obj) {
        ResourceLocation id = obj.getId();
        if( LABEL_RENDERERS.containsKey(id) ) {
            TmrConstants.LOG.log(Level.ERROR, String.format("The TCU label %s is already registered!", id), new InvalidParameterException());
            return;
        }

        LABEL_RENDERERS.put(id, obj);
        LABEL_RENDERER_LIST.add(obj);

        LABEL_RENDERER_LIST.sort(Comparator.comparingInt(ILabelRenderer::getSortOrder));
    }

    @Nonnull
    @Override
    public Collection<ILabelRenderer> getAll() {
        return LABEL_RENDERER_VIEW;
    }

    @Nonnull
    @Override
    public ILabelRenderer get(ResourceLocation id) {
        return LABEL_RENDERERS.getOrDefault(id, EMPTY);
    }

    @Nonnull
    @Override
    public ILabelRenderer getDefault() {
        return EMPTY;
    }

    public void render(Minecraft mc, WorldRenderer context, MatrixStack mat, float partialTicks, ActiveRenderInfo cameraEntity) {
        if( mc.player == mc.getCameraEntity() ) {
            TurretEntity turret = mc.crosshairPickEntity instanceof TurretEntity ? (TurretEntity) mc.crosshairPickEntity : null;

            boolean tcuHeld = TurretControlUnit.isTcuHeld(mc.player);
            boolean hasActive = false;
            for( int i = this.currentLabels.size() - 1; i >= 0; i-- ) {
                Label l = this.currentLabels.get(i);
                l.setActive(turret, tcuHeld);
                if( l.isDisposed() ) {
                    this.currentLabels.remove(i);
                } else if( l.isActive() ) {
                    hasActive = true;
                }
            }

            if( !hasActive && turret != null && tcuHeld ) {
                this.currentLabels.add(new Label(turret));
            }

            this.currentLabels.forEach(l -> l.render(cameraEntity, context, mat, partialTicks));
        }
    }

    @Override
    public FontRenderer getFontRenderer() {
        return Minecraft.getInstance().font;
    }

    @Override
    public int drawFont(String s, float x, float y, int color, MatrixStack matrixStack) {
        return drawFont(this.getFontRenderer(), s, color,
                        (fr, buf, txt, c) -> fr.drawInBatch(txt, x, y, c, false, matrixStack.last().pose(), buf, true, 0, 0xF000F0));
    }

    @Override
    public int drawFont(ITextComponent t, float x, float y, int color, MatrixStack matrixStack) {
        return drawFont(this.getFontRenderer(), t, color,
                        (fr, buf, txt, c) -> fr.drawInBatch(txt, x, y, c, false, matrixStack.last().pose(), buf, true, 0, 0xF000F0));
    }

    private static <T> int drawFont(FontRenderer fr, T t, int color, DrawFunc<T> draw) {
        IRenderTypeBuffer.Impl rtb = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());

        color = color & 0xFFFFFF | ((Math.max(4, (color >> 24) & 0xFF) << 24) & 0xFF000000);
        int w = draw.apply(fr, rtb, t, color);
        rtb.endBatch();

        RenderSystem.enableBlend();

        return w;
    }

    @Override
    public void quadPC(BufferBuilder buf, Matrix4f pose, Vector2f pos, Vector2f size, int color) {
        ColorObj c = new ColorObj(color);
        int      r = c.red(), g = c.green(), b = c.blue(), a = c.alpha();

        buf.vertex(pose, pos.x,          pos.y,          0).color(r, g, b, a).endVertex();
        buf.vertex(pose, pos.x,          pos.y + size.y, 0).color(r, g, b, a).endVertex();
        buf.vertex(pose, pos.x + size.x, pos.y + size.y, 0).color(r, g, b, a).endVertex();
        buf.vertex(pose, pos.x + size.x, pos.y,          0).color(r, g, b, a).endVertex();
    }

    @Override
    public void quadPT(BufferBuilder buf, Matrix4f pose, Vector2f pos, Vector2f size, Vector2f uv, Vector2f uvSize) {
        buf.vertex(pose, pos.x,          pos.y,          0).uv(uv.x,            uv.y)           .endVertex();
        buf.vertex(pose, pos.x,          pos.y + size.y, 0).uv(uv.x,            uv.y + uvSize.y).endVertex();
        buf.vertex(pose, pos.x + size.x, pos.y + size.y, 0).uv(uv.x + uvSize.x, uv.y + uvSize.y).endVertex();
        buf.vertex(pose, pos.x + size.x, pos.y,          0).uv(uv.x + uvSize.x, uv.y)           .endVertex();
    }

    @Override
    public void quadPCT(BufferBuilder buf, Matrix4f pose, Vector2f pos, Vector2f size, int color, Vector2f uv, Vector2f uvSize) {
        ColorObj c = new ColorObj(color);
        int      r = c.red(), g = c.green(), b = c.blue(), a = c.alpha();

        buf.vertex(pose, pos.x,          pos.y,          0).color(r, g, b, a).uv(uv.x,            uv.y)           .endVertex();
        buf.vertex(pose, pos.x,          pos.y + size.y, 0).color(r, g, b, a).uv(uv.x,            uv.y + uvSize.y).endVertex();
        buf.vertex(pose, pos.x + size.x, pos.y + size.y, 0).color(r, g, b, a).uv(uv.x + uvSize.x, uv.y + uvSize.y).endVertex();
        buf.vertex(pose, pos.x + size.x, pos.y,          0).color(r, g, b, a).uv(uv.x + uvSize.x, uv.y)           .endVertex();
    }

    public static void register(ILabelRegistry registry) {
        registry.register(new Name(new ResourceLocation(TmrConstants.ID, "name")));
        registry.register(new Health(new ResourceLocation(TmrConstants.ID, "health")));
        registry.register(new Ammo(new ResourceLocation(TmrConstants.ID, "ammo")));
        registry.register(new Target(new ResourceLocation(TmrConstants.ID, "target")));
    }

    public void cleanupRenderers() {
        this.currentLabels.clear();
    }

    private static class Label
    {
        private static final float OPACITY_CHANGE = 0.1F;

        private final WeakReference<TurretEntity> turretRef;
        private       float                       opacity;
        private boolean active;

        Label(TurretEntity turret) {
            this.turretRef = new WeakReference<>(turret);
            this.opacity = 0.0F;
            this.active = true;
        }

        boolean isDisposed() {
            return !this.active && this.opacity <= 0.0F;
        }

        boolean isActive() {
            return this.active;
        }

        void setActive(TurretEntity pointed, boolean tcuHeld) {
            this.active = pointed != null && this.getTurret() == pointed && tcuHeld;
        }

        TurretEntity getTurret() {
            return this.turretRef.get();
        }

        private int get(BiFunction<ILabelRenderer, TurretEntity, Integer> f, BinaryOperator<Integer> accumulator) {
            if( this.active || this.opacity > 0.0F ) {
                TurretEntity turret = this.getTurret();
                if( turret != null ) {
                    return LABEL_RENDERER_VIEW.stream().map(l -> f.apply(l, turret)).reduce(0, accumulator);
                }
            }

            return 0;
        }

        void render(ActiveRenderInfo camera, WorldRenderer context, MatrixStack mat, float partialTicks) {
            boolean doRender = false;
            if( !this.active && this.opacity > 0.0F ) {
                this.opacity -= OPACITY_CHANGE * partialTicks;
                doRender = true;
            } else if( this.active ) {
                if( this.opacity < 1.0F ) {
                    this.opacity += OPACITY_CHANGE * partialTicks;
                }
                doRender = true;
            }

            if( doRender ) {
                TurretEntity turret = this.getTurret();

                if( turret == null ) {
                    return;
                }

                final float alpha = MathHelper.clamp(this.opacity, 0.0F, 1.0F);
                final int totalWidth = this.get((iLabelRenderer1, turret2) -> iLabelRenderer1.getMinWidth(INSTANCE, turret2), Integer::max);
                final int totalHeight = this.get((iLabelRenderer, turret1) -> iLabelRenderer.getHeight(INSTANCE, turret1) + 2, Integer::sum) - 2;

                Vector3d camPos = turret.getPosition(partialTicks).subtract(camera.getPosition());
                int alphaBg = (Math.round(0xA0 * alpha) << 24) & 0xFF000000;
                float scale = 0.0075F * (float) camPos.length();

                mat.pushPose();
                mat.translate(camPos.x, camPos.y, camPos.z);
                mat.scale(scale, scale, scale);
                mat.mulPose(camera.rotation());
                mat.mulPose(TransformationHelper.quatFromXYZ(new Vector3f(0, 0, 180), true));
                mat.translate((totalWidth) / -2.0F, -totalHeight, 0.0F);

                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.disableTexture();

                Tessellator tess = Tessellator.getInstance();
                BufferBuilder buf = tess.getBuilder();
                buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

                int tw = totalWidth + 8, th = totalHeight + 8;
                Matrix4f pose = mat.last().pose();
                INSTANCE.quadPC(buf, pose, new Vector2f(1, 0),      new Vector2f(tw - 2, 1), 0x00343048 | alphaBg);
                INSTANCE.quadPC(buf, pose, new Vector2f(0, 1),      new Vector2f(1, th - 2), 0x00343048 | alphaBg);
                INSTANCE.quadPC(buf, pose, new Vector2f(1, th - 1), new Vector2f(tw - 2, 1), 0x00343048 | alphaBg);
                INSTANCE.quadPC(buf, pose, new Vector2f(tw - 1, 1), new Vector2f(1, th - 2), 0x00343048 | alphaBg);

                INSTANCE.quadPC(buf, pose, new Vector2f(1, 1),      new Vector2f(tw - 2, 1), 0x008880DD | alphaBg);
                INSTANCE.quadPC(buf, pose, new Vector2f(1, 2),      new Vector2f(1, th - 4), 0x008880DD | alphaBg);
                INSTANCE.quadPC(buf, pose, new Vector2f(1, th - 2), new Vector2f(tw - 2, 1), 0x008880DD | alphaBg);
                INSTANCE.quadPC(buf, pose, new Vector2f(tw - 2, 2), new Vector2f(1, th - 4), 0x008880DD | alphaBg);

                INSTANCE.quadPC(buf, pose, new Vector2f(2, 2), new Vector2f(tw - 4, th - 4), 0x00151019 | alphaBg);
                tess.end();

                mat.translate(4, 4, 0);
                LABEL_RENDERER_VIEW.stream().filter(l -> l.isVisible(turret)).forEach(l -> {
                    l.render(INSTANCE, turret, context, mat, totalWidth, totalHeight, partialTicks, alpha);
                    mat.translate(0, l.getHeight(INSTANCE, turret) + 2, 0);
                });

                RenderSystem.enableTexture();
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();

                mat.popPose();
            }
        }
    }

    @FunctionalInterface
    private interface DrawFunc<T>
    {
        int apply(FontRenderer fr, IRenderTypeBuffer buf, T txt, int color);
    }
}
