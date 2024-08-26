package me.Padej_.windowcleanersimulator.block;

import me.Padej_.windowcleanersimulator.block.custom.CustomGlassBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;

public class ModBlocks {
    public static final Block CLEAN_GLASS = RegisterBlocks.register(new CustomGlassBlock(FabricBlockSettings.copy(Blocks.GLASS).strength(.3f).nonOpaque(), Pollutable.PollutionLevel.CLEAN), "clean_glass");
    public static final Block SLIGHTLY_DIRTY_GLASS = RegisterBlocks.register(new CustomGlassBlock(FabricBlockSettings.copy(Blocks.GLASS).strength(.3f).nonOpaque(), Pollutable.PollutionLevel.SLIGHTLY_DIRTY), "slightly_dirty_glass");
    public static final Block DIRTY_GLASS = RegisterBlocks.register(new CustomGlassBlock(FabricBlockSettings.copy(Blocks.GLASS).strength(.3f).nonOpaque(), Pollutable.PollutionLevel.DIRTY), "dirty_glass");
    public static final Block FILTHY_GLASS = RegisterBlocks.register(new CustomGlassBlock(FabricBlockSettings.copy(Blocks.GLASS).strength(.3f).nonOpaque(), Pollutable.PollutionLevel.FILTHY), "filthy_glass");
}
