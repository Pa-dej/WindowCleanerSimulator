package me.Padej_.windowcleanersimulator.mixin;

import me.Padej_.windowcleanersimulator.util.WidgetsForInventoryMixin;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class MixinInventory extends Screen {

    protected MixinInventory() {
        super(Text.of(""));
    }

    private TexturedButtonWidget replaceButton;
    private TexturedButtonWidget undoButton;

    @Inject(at = @At("TAIL"), method = "init")
    private void addCustomButton(CallbackInfo ci) {
        WidgetsForInventoryMixin buttons = new WidgetsForInventoryMixin(this.width, this.height);
        this.replaceButton = buttons.createReplaceButton(this.width - 20, 10);
        this.undoButton = buttons.createUndoButton(this.width - 40, 10);
        this.addDrawableChild(this.replaceButton);
        this.addDrawableChild(this.undoButton);
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void updateButtonPosition(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {

        if (this.client != null && this.client.player != null) {

            if (this.replaceButton != null && this.undoButton != null) {
                this.replaceButton.setPosition(this.width - 20, 10);
                this.undoButton.setPosition(this.width - 40, 10);
            }
        }
    }
}
