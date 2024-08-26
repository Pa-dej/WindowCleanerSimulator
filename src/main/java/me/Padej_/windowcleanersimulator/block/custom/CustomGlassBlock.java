package me.Padej_.windowcleanersimulator.block.custom;

import me.Padej_.windowcleanersimulator.block.Pollutable;
import net.minecraft.block.BlockState;
import net.minecraft.block.TransparentBlock;
import net.minecraft.util.math.Direction;

import java.util.Optional;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class CustomGlassBlock extends TransparentBlock implements Pollutable {
    private final PollutionLevel pollutionLevel;

    public CustomGlassBlock(Settings settings, PollutionLevel pollutionLevel) {
        super(settings);
        this.pollutionLevel = pollutionLevel;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (adjacentBlockState.getBlock() instanceof TransparentBlock) {
            return true;
        }
        return super.isSideInvisible(state, adjacentBlockState, side);
    }

    @Override
    public Optional<BlockState> getDegradationResult(BlockState state) {
        return Pollutable.super.getDegradationResult(state);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);

        if (random.nextFloat() < getDegradationChanceMultiplier()) {
            getDegradationResult(state).ifPresent(newState -> {
                world.setBlockState(pos, newState);
            });
        }
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public float getDegradationChanceMultiplier() {
        return 0.2f; // 20% шанс
    }

    @Override
    public PollutionLevel getDegradationLevel() {
        return this.pollutionLevel;
    }
}