package me.Padej_.windowcleanersimulator.mixin;

import me.Padej_.windowcleanersimulator.util.WidgetsForInventoryMixin;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
public class MixinCreativeInventory extends Screen {

    protected MixinCreativeInventory() {
        super(Text.of(""));
    }

    private TexturedButtonWidget replaceButton;
    private TexturedButtonWidget undoButton;
    private boolean isButtonAdded = false;

    @Inject(at = @At("TAIL"), method = "init")
    private void addCustomButton(CallbackInfo ci) {
        updateButton();
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void onRender(CallbackInfo ci) {
        updateButton();
    }

    private void updateButton() {
        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;

        // Добавляем кнопку, если вкладка "Инвентарь" выбрана
        if (screen.isInventoryTabSelected() && !isButtonAdded) {
            WidgetsForInventoryMixin buttons = new WidgetsForInventoryMixin(this.width, this.height);
            this.replaceButton = buttons.createReplaceButton(this.width - 20, 10);
            this.undoButton = buttons.createUndoButton(this.width - 40, 10);
            this.addDrawableChild(this.replaceButton);
            this.addDrawableChild(this.undoButton);
            isButtonAdded = true;
        }

        // Удаляем кнопку, если вкладка не "Инвентарь"
        if (!screen.isInventoryTabSelected() && isButtonAdded) {
            this.remove(this.replaceButton);
            this.remove(this.undoButton);
            isButtonAdded = false;
        }
    }
}
