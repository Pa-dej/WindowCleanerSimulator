package me.Padej_.windowcleanersimulator.util;

import me.Padej_.windowcleanersimulator.client.WindowCleanerSimulatorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.util.Identifier;

import static me.Padej_.windowcleanersimulator.WindowCleanerSimulator.MOD_ID;

public record WidgetsForInventoryMixin(int width, int height) {

    private static final Identifier SPONGE_FOCUSED = Identifier.of(MOD_ID, "sponge/focused");
    private static final Identifier SPONGE_UNFOCUSED = Identifier.of(MOD_ID, "sponge/unfocused");

    private static final Identifier UNDO_FOCUSED = Identifier.of(MOD_ID, "undo/focused");
    private static final Identifier UNDO_UNFOCUSED = Identifier.of(MOD_ID, "undo/unfocused");

    public TexturedButtonWidget createReplaceButton(int x, int y) {
        return new TexturedButtonWidget(x, y, 16, 16, new ButtonTextures(SPONGE_UNFOCUSED, SPONGE_FOCUSED), (btn) -> {
            WindowCleanerSimulatorClient.spawnSponge();
            WindowCleanerSimulatorClient.replaceAllGlassWithCleanGlass(MinecraftClient.getInstance().world);
        });
    }

    public TexturedButtonWidget createUndoButton(int x, int y) {
        return new TexturedButtonWidget(x, y, 16, 16, new ButtonTextures(UNDO_UNFOCUSED, UNDO_FOCUSED), (btn) -> {
            WindowCleanerSimulatorClient.removeSponge();
            WindowCleanerSimulatorClient.replaceAllCleanGlassWithGlass(MinecraftClient.getInstance().world);
        });
    }
}
