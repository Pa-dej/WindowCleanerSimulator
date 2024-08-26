package me.Padej_.windowcleanersimulator.block;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static me.Padej_.windowcleanersimulator.WindowCleanerSimulator.MOD_ID;

public final class RegisterBlocks {

    public static Block register(Block block, String path) {
        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, path), block);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, path), new BlockItem(block, new Item.Settings()));

        return block;
    }

    public static void initialize() {
    }
}
