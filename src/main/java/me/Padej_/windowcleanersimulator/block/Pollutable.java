package me.Padej_.windowcleanersimulator.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Degradable;
import net.minecraft.util.StringIdentifiable;

import java.util.Optional;
import java.util.function.Supplier;

public interface Pollutable extends Degradable<Pollutable.PollutionLevel> {
    Supplier<BiMap<Block, Block>> POLLUTION_LEVEL_INCREASES = Suppliers.memoize(() -> ImmutableBiMap.<Block, Block>builder()
            .put(ModBlocks.CLEAN_GLASS, ModBlocks.SLIGHTLY_DIRTY_GLASS)
            .put(ModBlocks.SLIGHTLY_DIRTY_GLASS, ModBlocks.DIRTY_GLASS)
            .put(ModBlocks.DIRTY_GLASS, ModBlocks.FILTHY_GLASS)
            .build());

    Supplier<BiMap<Block, Block>> POLLUTION_LEVEL_DECREASES = Suppliers.memoize(() -> POLLUTION_LEVEL_INCREASES.get().inverse());

    static Optional<Block> getDecreasedPollutionBlock(Block block) {
        return Optional.ofNullable(POLLUTION_LEVEL_DECREASES.get().get(block));
    }

    static Optional<BlockState> getDecreasedPollutionState(BlockState state) {
        return getDecreasedPollutionBlock(state.getBlock()).map(block -> block.getStateWithProperties(state));
    }

    static Optional<Block> getIncreasedPollutionBlock(Block block) {
        return Optional.ofNullable(POLLUTION_LEVEL_INCREASES.get().get(block));
    }

    default Optional<BlockState> getDegradationResult(BlockState state) {
        return getIncreasedPollutionBlock(state.getBlock()).map(block -> block.getStateWithProperties(state));
    }

    enum PollutionLevel implements StringIdentifiable {
        CLEAN("clean"),
        SLIGHTLY_DIRTY("slightly_dirty"),
        DIRTY("dirty"),
        FILTHY("filthy");

        private final String id;

        PollutionLevel(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }
    }
}
