/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.gui.controls;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.montoyo.wd.client.gui.WDScreen;
import net.montoyo.wd.client.gui.loading.JsonOWrapper;
import net.montoyo.wd.utilities.Bounds;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

@OnlyIn(Dist.CLIENT)
public abstract class Control {

    public static final int COLOR_BLACK    = 0xFF000000;
    public static final int COLOR_WHITE    = 0xFFFFFFFF;
    public static final int COLOR_RED      = 0xFFFF0000;
    public static final int COLOR_GREEN    = 0xFF00FF00;
    public static final int COLOR_BLUE     = 0xFF0000FF;
    public static final int COLOR_CYAN     = 0xFF00FFFF;
    public static final int COLOR_MANGENTA = 0xFFFF00FF;
    public static final int COLOR_YELLOW   = 0xFFFFFF00;

    protected final Minecraft mc;
    protected final Font font;
    protected final Tesselator tessellator;
    protected final BufferBuilder vBuffer;
    protected static WDScreen parent;
    protected String name;
    protected Object userdata;

    public Control() {
        mc = Minecraft.getInstance();
        font = mc.font;
        tessellator = Tesselator.getInstance();
        vBuffer = tessellator.getBuilder();
        parent = WDScreen.CURRENT_SCREEN;
    }

    public Object getUserdata() {
        return userdata;
    }

    public void setUserdata(Object userdata) {
        this.userdata = userdata;
    }

    public boolean keyTyped(int keyCode, int modifier) {
        return false;
    }

    public boolean keyUp(int key, int scanCode, int modifiers) {
        return false;
    }

    public boolean keyDown(int key, int scanCode, int modifiers) {
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        return false;
    }

    public boolean mouseClickMove(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    public boolean mouseMove(double mouseX, double mouseY) {
        return false;
    }

    public boolean mouseScroll(double mouseX, double mouseY, double amount) {
        return false;
    }

    public void draw(GuiGraphics poseStack, int mouseX, int mouseY, float ptt) {
    }

    public void postDraw(GuiGraphics poseStack, int mouseX, int mouseY, float ptt) {
    }

    public void destroy() {
    }

    public WDScreen getParent() {
        return parent;
    }

    public abstract int getX();
    public abstract int getY();
    public abstract int getWidth();
    public abstract int getHeight();
    public abstract void setPos(int x, int y);

    public void fillRect(int x, double y, int w, int h, int color) {
        double x1 = (double) x;
        double y1 = (double) y;
        double x2 = (double) (x + w);
        double y2 = (double) (y + h);
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8 ) & 0xFF;
        int b =  color & 0xFF;

        RenderSystem.setShaderColor(((float) r) / 255.f, ((float) g) / 255.f, ((float) b) / 255.f, ((float) a) / 255.f);
//        RenderSystem.disableTexture();
        glDisable(GL_TEXTURE_2D);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        vBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        vBuffer.vertex(x1, y2, 0.0).endVertex();
        vBuffer.vertex(x2, y2, 0.0).endVertex();
        vBuffer.vertex(x2, y1, 0.0).endVertex();
        vBuffer.vertex(x1, y1, 0.0).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
//        RenderSystem.enableTexture();
        glEnable(GL_TEXTURE_2D);
    }

    public void fillTexturedRect(PoseStack poseStack, int x, int y, int w, int h, double u1, double v1, double u2, double v2) {
        double x1 = (double) x;
        double y1 = (double) y;
        double x2 = (double) (x + w);
        double y2 = (double) (y + h);

        vBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        vBuffer.vertex(x1, y2, 0.0).uv((float) u1, (float) v2).color(255, 255, 255, 255).endVertex();
        vBuffer.vertex(x2, y2, 0.0).uv((float) u2, (float) v2).color(255, 255, 255, 255).endVertex();
        vBuffer.vertex(x2, y1, 0.0).uv((float) u2, (float) v1).color(255, 255, 255, 255).endVertex();
        vBuffer.vertex(x1, y1, 0.0).uv((float) u1, (float) v1).color(255, 255, 255, 255).endVertex();
        tessellator.end();
    }

    public static void blend(boolean enable) {
        if(enable) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
        } else
            RenderSystem.disableBlend();
    }

    public void bindTexture(ResourceLocation resLoc) {
        if(resLoc == null)
            RenderSystem.setShaderTexture(0, 0); //Damn state manager
        else
            RenderSystem.setShaderTexture(0, resLoc);
    }

    public void drawBorder(PoseStack poseStack, int x, int y, int w, int h, int color) {
        drawBorder(poseStack, x, y, w, h, color, 1.0);
    }

    public void drawBorder(PoseStack poseStack, int x, int y, int w, int h, int color, double sz) {
        double x1 = (double) x;
        double y1 = (double) y;
        double x2 = (double) (x + w);
        double y2 = (double) (y + h);
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8 ) & 0xFF;
        int b =  color & 0xFF;

        RenderSystem.setShaderColor(((float) r) / 255.f, ((float) g) / 255.f, ((float) b) / 255.f, ((float) a) / 255.f);
//        RenderSystem.enableTexture();
        glEnable(GL_TEXTURE_2D);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        vBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        //Top edge (y = y1)
        vBuffer.vertex(x1, y1 + sz, 0.0).endVertex();
        vBuffer.vertex(x2, y1 + sz, 0.0).endVertex();
        vBuffer.vertex(x2, y1, 0.0).endVertex();
        vBuffer.vertex(x1, y1, 0.0).endVertex();

        //Bottom edge (y = y2)
        vBuffer.vertex(x1, y2, 0.0).endVertex();
        vBuffer.vertex(x2, y2, 0.0).endVertex();
        vBuffer.vertex(x2, y2 - sz, 0.0).endVertex();
        vBuffer.vertex(x1, y2 - sz, 0.0).endVertex();

        //Left edge (x = x1)
        vBuffer.vertex(x1, y2, 0.0).endVertex();
        vBuffer.vertex(x1 + sz, y2, 0.0).endVertex();
        vBuffer.vertex(x1 + sz, y1, 0.0).endVertex();
        vBuffer.vertex(x1, y1, 0.0).endVertex();

        //Right edge (x = x2)
        vBuffer.vertex(x2 - sz, y2, 0.0).endVertex();
        vBuffer.vertex(x2, y2, 0.0).endVertex();
        vBuffer.vertex(x2, y1, 0.0).endVertex();
        vBuffer.vertex(x2 - sz, y1, 0.0).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
//        RenderSystem.enableTexture();
        glEnable(GL_TEXTURE_2D);
    }

    public PoseStack beginFramebuffer(RenderTarget fbo, float vpW, float vpH) {
        fbo.bindWrite(true);

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f().ortho(0.0f, vpW, vpH, 0.0f, -1.0f,1.0f), VertexSorting.ORTHOGRAPHIC_Z);

        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.setIdentity();
//        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0f));
        RenderSystem.applyModelViewMatrix();

        if(!fbo.useDepth)
            RenderSystem.disableDepthTest();

        return poseStack;
    }

    public void endFramebuffer(PoseStack poseStack, RenderTarget fbo) {
        if(!fbo.useDepth)
            RenderSystem.enableDepthTest();


        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.restoreProjectionMatrix();
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        fbo.unbindWrite();
        mc.getMainRenderTarget().bindWrite(true);
    }

    public static String tr(String text) {
        if(text.length() >= 2 && text.charAt(0) == '$') {
            if(text.charAt(1) == '$')
                return text.substring(1);
            else
                return I18n.get(text.substring(1));
        } else
            return text;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void load(JsonOWrapper json) {
        name = json.getString("name", "");
    }

    public static Bounds findBounds(java.util.List<Control> controlList) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for(Control ctrl : controlList) {
            int x = ctrl.getX();
            int y = ctrl.getY();
            if(x < minX)
                minX = x;

            if(y < minY)
                minY = y;

            x += ctrl.getWidth();
            y += ctrl.getHeight();

            if(x > maxX)
                maxX = x;

            if(y >= maxY)
                maxY = y;
        }

        return new Bounds(minX, minY, maxX, maxY);
    }

}
