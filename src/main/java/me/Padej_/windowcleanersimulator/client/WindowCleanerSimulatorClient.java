package me.Padej_.windowcleanersimulator.client;

import me.Padej_.windowcleanersimulator.block.ModBlocks;
import me.Padej_.windowcleanersimulator.block.custom.CustomGlassBlock;
import me.Padej_.windowcleanersimulator.entity.ModEntities;
import me.Padej_.windowcleanersimulator.entity.Sponge;
import me.Padej_.windowcleanersimulator.entity.model.SpongeModel;
import me.Padej_.windowcleanersimulator.entity.render.SpongeRenderer;
import me.Padej_.windowcleanersimulator.particle.BubblePopParticle;
import me.Padej_.windowcleanersimulator.particle.SparkParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static me.Padej_.windowcleanersimulator.WindowCleanerSimulator.*;

public class WindowCleanerSimulatorClient implements ClientModInitializer {
    public static final EntityModelLayer SPONGE_LAYER = new EntityModelLayer(Identifier.of(MOD_ID, "sponge"), "main");
    public static Sponge sponge;
    private static final int UPDATE_INTERVAL_TICKS = 200;
    private static final float DEGRADATION_CHANCE = 0.2f; // Шанс загрязнение множества блоков
    public static final Map<BlockPos, Integer> recentlyCleanedBlocks = new HashMap<>();
    private static final int CLEANING_COOLDOWN_TICKS = 6400;

    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.SPONGE, SpongeRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(SPONGE_LAYER, SpongeModel::getTexturedModelData);

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CLEAN_GLASS, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SLIGHTLY_DIRTY_GLASS, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DIRTY_GLASS, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.FILTHY_GLASS, RenderLayer.getTranslucent());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            ClientWorld world = minecraftClient.world;

            if (world != null && minecraftClient.player != null) {
                tickCounter++;
                if (tickCounter >= UPDATE_INTERVAL_TICKS) {
                    tickCounter = 0;
                    updatePollutionForPlayer(MinecraftClient.getInstance().world, minecraftClient.player.getBlockPos());
                }
            }
        });

        ParticleFactoryRegistry.getInstance().register(SPARK, SparkParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(POP_BUBBLE, BubblePopParticle.Factory::new);
    }

    private void updatePollutionForPlayer(ClientWorld world, BlockPos playerPos) {
        int radius = 48;
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        Random random = new Random();  // Используем стандартный Random

        // Проходим по всем блокам в радиусе от игрока
        for (int x = -radius; x <= radius; x ++) {
            for (int y = -radius / 4; y <= radius / 4; y ++) {
                for (int z = -radius; z <= radius; z ++) {
                    mutablePos.set(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);

                    BlockState state = world.getBlockState(mutablePos);
                    Block block = state.getBlock();

                    // Проверяем, является ли блок одним из стеклянных блоков и не был ли очищен недавно
                    if (block instanceof CustomGlassBlock customGlassBlock) {
                        if (recentlyCleanedBlocks.containsKey(mutablePos)) {
                            int cleanedTime = recentlyCleanedBlocks.get(mutablePos);
                            if (world.getTime() - cleanedTime < CLEANING_COOLDOWN_TICKS) {
                                continue; // Пропускаем этот блок, если он не прошел время кулида
                            } else {
                                recentlyCleanedBlocks.remove(mutablePos); // Удаляем запись, если срок охлаждения истек
                            }
                        }

                        // Проверяем вероятность загрязнения
                        if (random.nextFloat() < DEGRADATION_CHANCE) {
                            BlockState newState = customGlassBlock.getDegradationResult(state).orElse(null);

                            if (newState != null && !state.equals(newState)) {
                                world.setBlockState(mutablePos, newState, 3); // Обновляем блок
                            }
                        }
                    }
                }
            }
        }
    }

    public static Sponge spawnSponge() {
        sponge = new Sponge(ModEntities.SPONGE, MinecraftClient.getInstance().world);

        Vec3d pos = MinecraftClient.getInstance().player.getPos();
        sponge.updateTrackedPosition(pos.getX(), pos.getY(), pos.getZ());
        sponge.refreshPositionAfterTeleport(pos.getX(), pos.getY(), pos.getZ());
        sponge.setId(3308);

        MinecraftClient.getInstance().world.addEntity(sponge);

        return sponge;
    }

    public static void removeSponge() {
        if (sponge == null) return;
        MinecraftClient.getInstance().world.removeEntity(3308, Entity.RemovalReason.KILLED);
    }

    public static void replaceAllGlassWithCleanGlass(ClientWorld world) {
        BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();

        for (int x = playerPos.getX() - 100; x <= playerPos.getX() + 100; x++) {
            for (int z = playerPos.getZ() - 100; z <= playerPos.getZ() + 100; z++) {
                for (int y = playerPos.getY() - 50; y <= playerPos.getY() + 50; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    // Проверяем, является ли текущий блок стеклом
                    if (state.getBlock() == Blocks.GLASS) {
                        // Меняем его на чистое стекло
                        world.setBlockState(pos, ModBlocks.CLEAN_GLASS.getDefaultState(), 3);
                    }
                }
            }
        }
    }

    public static void replaceAllCleanGlassWithGlass(ClientWorld world) {
        BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();

        for (int x = playerPos.getX() - 100; x <= playerPos.getX() + 100; x++) {
            for (int z = playerPos.getZ() - 100; z <= playerPos.getZ() + 100; z++) {
                for (int y = playerPos.getY() - 50; y <= playerPos.getY() + 50; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    // Проверяем, является ли текущий блок стеклом
                    if (state.getBlock() == ModBlocks.CLEAN_GLASS || state.getBlock() == ModBlocks.SLIGHTLY_DIRTY_GLASS || state.getBlock() == ModBlocks.DIRTY_GLASS || state.getBlock() == ModBlocks.DIRTY_GLASS) {
                        // Меняем его на чистое стекло
                        world.setBlockState(pos, Blocks.GLASS.getDefaultState());
                    }
                }
            }
        }
    }
}
