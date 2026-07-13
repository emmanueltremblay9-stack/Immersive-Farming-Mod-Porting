package dev.emmanueltremblay.immersivefarming.client.gui;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.menu.ComposterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ComposterScreen extends AbstractContainerScreen<ComposterMenu> {
    private static final ResourceLocation TEXTURE = ImmersiveFarming.id("textures/gui/composter.png");
    private static final int WATER_COLOR = 0xAA3F76E4;
    private static final int WET_MATTER_COLOR = 0xAA4B9D32;
    private static final int DRY_MATTER_COLOR = 0xAA8A5B2E;
    private static final int ENERGY_COLOR = 0xFFFF9A2A;

    public ComposterScreen(ComposterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawTank(guiGraphics, 55, 20, menu.wetMatterAmount(), menu.wetMatterCapacity(), WET_MATTER_COLOR);
        drawTank(guiGraphics, 82, 20, menu.dryMatterAmount(), menu.dryMatterCapacity(), DRY_MATTER_COLOR);
        drawTank(guiGraphics, 109, 20, menu.waterAmount(), menu.waterCapacity(), WATER_COLOR);
        drawEnergy(guiGraphics);
        drawProcess(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderGaugeTooltip(guiGraphics, mouseX, mouseY);
    }

    private void drawTank(GuiGraphics guiGraphics, int x, int y, int amount, int capacity, int color) {
        int fillHeight = scaled(amount, capacity, 47);
        if (fillHeight > 0) {
            int left = leftPos + x;
            int top = topPos + y + 47 - fillHeight;
            guiGraphics.fill(left, top, left + 16, top + fillHeight, color);
        }
        guiGraphics.blit(TEXTURE, leftPos + x - 2, topPos + y - 2, 177, 31, 20, 51);
    }

    private void drawEnergy(GuiGraphics guiGraphics) {
        int fillHeight = scaled(menu.energy(), menu.energyCapacity(), 47);
        if (fillHeight > 0) {
            int left = leftPos + 158;
            int top = topPos + 21 + 47 - fillHeight;
            guiGraphics.fill(left, top, left + 6, top + fillHeight, ENERGY_COLOR);
        }
    }

    private void drawProcess(GuiGraphics guiGraphics) {
        int fillHeight = scaled(menu.processTime(), menu.processTimeTotal(), 16);
        if (fillHeight > 0) {
            guiGraphics.blit(TEXTURE, leftPos + 24, topPos + 7 + 16 - fillHeight, 176, 16 - fillHeight, 2, fillHeight);
        }
    }

    private void renderGaugeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovering(55, 20, 16, 47, mouseX, mouseY)) {
            renderTankTooltip(guiGraphics, mouseX, mouseY, Component.translatable("fluid.immersive_farming_mod_porting.wet_matter_fluid"),
                    menu.wetMatterAmount(), menu.wetMatterCapacity());
        } else if (isHovering(82, 20, 16, 47, mouseX, mouseY)) {
            renderTankTooltip(guiGraphics, mouseX, mouseY, Component.translatable("fluid.immersive_farming_mod_porting.dry_matter_fluid"),
                    menu.dryMatterAmount(), menu.dryMatterCapacity());
        } else if (isHovering(109, 20, 16, 47, mouseX, mouseY)) {
            renderTankTooltip(guiGraphics, mouseX, mouseY, Component.translatable("block.minecraft.water"),
                    menu.waterAmount(), menu.waterCapacity());
        } else if (isHovering(157, 21, 8, 47, mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(font, List.of(Component.literal(menu.energy() + " FE / " + menu.energyCapacity() + " FE")),
                    mouseX, mouseY);
        }
    }

    private void renderTankTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, Component name, int amount, int capacity) {
        guiGraphics.renderComponentTooltip(font, List.of(name, Component.literal(amount + " mB / " + capacity + " mB")), mouseX, mouseY);
    }

    private static int scaled(int amount, int capacity, int pixels) {
        if (amount <= 0 || capacity <= 0) {
            return 0;
        }
        return Math.max(1, Math.min(pixels, amount * pixels / capacity));
    }
}
