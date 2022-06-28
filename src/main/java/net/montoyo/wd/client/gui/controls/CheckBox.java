/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.gui.controls;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.montoyo.wd.client.gui.loading.JsonOWrapper;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CheckBox extends BasicControl {

    private static final ResourceLocation texUnchecked = new ResourceLocation("webdisplays", "textures/gui/checkbox.png");
    private static final ResourceLocation texChecked = new ResourceLocation("webdisplays", "textures/gui/checkbox_checked.png");
    public static final int WIDTH = 16;
    public static final int HEIGHT = 16;

    public static class CheckedEvent extends Event<CheckBox> {

        private final boolean checked;

        public CheckedEvent(CheckBox cb) {
            source = cb;
            checked = cb.checked;
        }

        public boolean isChecked() {
            return checked;
        }

    }

    private String label;
    private int labelW;
    private boolean checked;
    private java.util.List<String> tooltip;

    public CheckBox() {
        label = "";
    }

    public CheckBox(int x, int y, String label) {
        this.label = label;
        labelW = font.width(label);
        checked = false;
        this.x = x;
        this.y = y;
    }

    public CheckBox(int x, int y, String label, boolean val) {
        this.label = label;
        labelW = font.width(label);
        checked = val;
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(mouseButton == 0 && !disabled) {
            if(mouseX >= x && mouseX <= x + WIDTH + 2 + labelW && mouseY >= y && mouseY < y + HEIGHT) {
                checked = !checked;
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                parent.actionPerformed(new CheckedEvent(this));
            }

            return true;
        }

        return false;
    }

    @Override
    public void draw(PoseStack poseStack, int mouseX, int mouseY, float ptt) {
        if(visible) {
//            GlStateManager.disableAlpha();
            poseStack.pushPose();
            RenderSystem.setShaderTexture(2, checked ? texChecked : texUnchecked);
            RenderSystem.bindTexture(2);
            RenderSystem.enableBlend();
            fillTexturedRect(poseStack, x, y, WIDTH, HEIGHT, 0.0, 0.0, 1.0, 1.0);
            RenderSystem.disableBlend();
            RenderSystem.bindTexture(-1);

            poseStack.popPose();
            boolean inside = (!disabled && mouseX >= x && mouseX <= x + WIDTH + 2 + labelW && mouseY >= y && mouseY < y + HEIGHT);
            font.draw(poseStack, label, x + WIDTH + 2, y + 4, inside ? 0xFF0080FF : COLOR_WHITE);
        }
    }

    public void setLabel(String label) {
        this.label = label;
        labelW = font.width(label);
    }

    public String getLabel() {
        return label;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public int getWidth() {
        return WIDTH + 2 + labelW;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void load(JsonOWrapper json) {
        super.load(json);
        label = tr(json.getString("label", ""));
        labelW = font.width(label);
        checked = json.getBool("checked", false);

        String tt = tr(json.getString("tooltip", ""));
        if(!tt.isEmpty()) {
            tooltip = Lists.newArrayList(tt.split("\\\\n"));
            parent.requirePostDraw(this);
        }
    }

    @Override
    public void postDraw(PoseStack poseStack, int mouseX, int mouseY, float ptt) {
        if(tooltip != null && !disabled && mouseX >= x && mouseX <= x + WIDTH + 2 + labelW && mouseY >= y && mouseY < y + HEIGHT)
            parent.drawTooltip(poseStack, tooltip, mouseX, mouseY);
    }

}
