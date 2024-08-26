package me.Padej_.windowcleanersimulator.entity;

import me.Padej_.windowcleanersimulator.block.Pollutable;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import static me.Padej_.windowcleanersimulator.WindowCleanerSimulator.POP_BUBBLE;
import static me.Padej_.windowcleanersimulator.WindowCleanerSimulator.SPARK;
import static me.Padej_.windowcleanersimulator.client.WindowCleanerSimulatorClient.recentlyCleanedBlocks;

public class Sponge extends PathAwareEntity {

    private boolean followingPlayer = false;
    private boolean goingUp = true; // Флаг для определения направления плавучести
    public static int wetnessTimer = 0; // Таймер мокрости, максимум 6000
    private static final double VELOCITY_THRESHOLD = 0.01; // Порог для определения "потирания"
    private static final double CHANGE_PROBABILITY = 0.3; // Вероятность смены стадии окисления (70%)

    public Sponge(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    private boolean isScrubbing() {
        Vec3d velocity = this.getVelocity();
        double averageVelocity = (velocity.x + velocity.y + velocity.z) / 3.0;
        return Math.abs(averageVelocity) > VELOCITY_THRESHOLD;
    }

    @Override
    public void tick() {

        super.tick();

        if (this.getWorld().isClient) {
            this.bodyTrackingIncrements = 0;
            this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());

            this.updateGoalControls();

            this.move(MovementType.SELF, this.getVelocity());
            Vec3d velocity = this.getVelocity();
            if (!isOnGround() && !followingPlayer && !isTouchingWater()) this.setVelocity(velocity.x / 2, velocity.y / 2 - 0.07, velocity.z / 2);
        }

        if (followingPlayer) {
            if (isScrubbing() && this.age % 15 == 0 && this.isWet()) {
                deoxidizeCopperBlocks();
                cleanGlassBlocks();
            }

            if (Math.round(this.getYaw()) != 0) {
                if (this.getYaw() < 0) this.setRotation(this.getYaw() + 1, this.getPitch());
                if (this.getYaw() > 0) this.setRotation(this.getYaw() - 1, this.getPitch());
            }

            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                Vec3d playerLook = player.getRotationVector(); // Получить вектор взгляда игрока
                Vec3d playerPos = player.getEyePos(); // Получить позицию глаз игрока
                Vec3d targetPos = playerPos.add(playerLook.multiply(3)).add(0, -this.getHeight() / 2, 0); // Смещаем цель на половину высоты сущности ниже

                Vec3d entityPos = this.getPos();
                double distanceToTarget = entityPos.distanceTo(targetPos); // Расстояние до цели

                // Ограничить расстояние до игрока до 3 блоков
                if (distanceToTarget > 3) {
                    targetPos = playerPos.add(playerLook.multiply(3)).add(0, -0.1, 0);
                    distanceToTarget = 3;
                }

                // Вычислить вектор перемещения
                Vec3d direction = targetPos.subtract(entityPos);

                // Рассчитать скорость от 1 до 0 в зависимости от расстояния до цели (нормализовано между 0 и 3)
                double speedFactor = distanceToTarget / 3.0; // Нормализованное значение от 0 до 1

                // Получаем скорость игрока
                double playerSpeed = player.getVelocity().length();

                // Увеличиваем максимальную скорость на скорость игрока
                double maxSpeed = 1.0 + playerSpeed;

                // Рассчитываем текущую скорость с учетом расстояния и скорости игрока
                double speed = maxSpeed * speedFactor;

                this.setVelocity(direction.normalize().multiply(speed)); // Перемещение с рассчитанной скоростью
                this.setNoGravity(true); // Отключить гравитацию
            }
        } else {
            this.setNoGravity(false); // Включить гравитацию, когда не следуем за игроком
        }

        // Проверка на нахождение в блоке котла с водой
        if (isInWaterCauldron()) {
            wetnessTimer = Math.min(wetnessTimer + 40, 6000); // Увеличиваем таймер мокрости на 40, максимум 6000
        } else if (this.isTouchingWater()) {
            wetnessTimer = Math.min(wetnessTimer + 10, 6000); // Увеличиваем таймер мокрости, максимум 6000
            applyBuoyancy();
        } else {
            int dryingRate;

            // Если значение таймера мокрости меньше или равно 50, устанавливаем скорость высыхания на 1
            if (wetnessTimer <= 50) {
                dryingRate = 1;
            } else {
                // Проверка на наличие тепловых блоков рядом
                boolean hasHeatSourceNearby = checkForHeatSource();
                dryingRate = hasHeatSourceNearby ? 40 : 1; // Увеличенная скорость высыхания, если есть источник тепла
            }

            // Уменьшаем таймер мокрости с учетом источников тепла
            if (!this.isTouchingWater()) {
                if (wetnessTimer > 0) wetnessTimer = Math.max(wetnessTimer - dryingRate, 0); // Уменьшаем таймер мокрости, минимум 0
            }

            // Проверка, если губка высыхает и начинает издавать звук
            if (wetnessTimer == 1) this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_WET_SPONGE_DRIES, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }

        // Обновление частиц воды и облака
        if (isWet()) spawnWaterParticles();
        if (wetnessTimer == 1) this.getWorld().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 0, 0.003, 0);
    }

    private boolean isInWaterCauldron() {
        BlockPos spongePos = this.getBlockPos();
        World world = this.getWorld();

        BlockState blockState = world.getBlockState(spongePos);

        if (blockState.getBlock() instanceof LeveledCauldronBlock) {
            int level = blockState.get(LeveledCauldronBlock.LEVEL);

            return level > 0;
        }
        return false;
    }


    private void deoxidizeCopperBlocks() {
        float radius = 1;
        BlockPos spongePos = this.getBlockPos();
        World world = this.getWorld();
        Random random = new Random();

        // Проходим по всем блокам в пределах радиуса
        for (float x = -radius; x <= radius; x++) {
            for (float y = -radius; y <= radius; y++) {
                for (float z = -radius; z <= radius; z++) {
                    BlockPos checkPos = spongePos.add((int) x, (int) y, (int) z);
                    BlockState blockState = world.getBlockState(checkPos);

                    if (blockState.getBlock() instanceof Oxidizable) {
                        if (random.nextDouble() < CHANGE_PROBABILITY) {
                            Optional<BlockState> deoxidizedBlockState = Oxidizable.getDecreasedOxidationState(blockState);
                            deoxidizedBlockState.ifPresent(newState -> world.setBlockState(checkPos, newState));
                            double offsetX = (this.random.nextDouble() - 0.5) * this.getWidth();
                            double offsetY = this.random.nextDouble() * this.getHeight();
                            double offsetZ = (this.random.nextDouble() - 0.5) * this.getWidth();

                            ParticleEffect particleEffect = (ParticleEffect) (random.nextDouble() < 0.3 ? POP_BUBBLE : SPARK);

                            // Добавляем частицу в мир
                            this.getWorld().addParticle(particleEffect, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, offsetX / 3, offsetY / 3, offsetZ / 3);
                        }
                    }
                }
            }
        }
    }

    private void cleanGlassBlocks() {
        float radius = 1.0f;
        BlockPos spongePos = this.getBlockPos();
        World world = this.getWorld();
        Random random = new Random();

        // Проходим по всем блокам в пределах радиуса
        for (int x = -Math.round(radius); x <= Math.round(radius); x++) {
            for (int y = -Math.round(radius); y <= Math.round(radius); y++) {
                for (int z = -Math.round(radius); z <= Math.round(radius); z++) {
                    BlockPos checkPos = spongePos.add(x, y, z);
                    BlockState blockState = world.getBlockState(checkPos);

                    if (blockState.getBlock() instanceof Pollutable) {
                        if (random.nextDouble() < CHANGE_PROBABILITY) {
                            Optional<BlockState> cleanedBlockState = Pollutable.getDecreasedPollutionState(blockState);
                            cleanedBlockState.ifPresent(newState -> {
                                world.setBlockState(checkPos, newState);
                                recentlyCleanedBlocks.put(checkPos, Math.toIntExact(world.getTime())); // Запоминаем очищенный блок и текущее время
                            });

                            // Создаем частицу для визуализации процесса
                            double offsetX = (random.nextDouble() - 0.5) * this.getWidth();
                            double offsetY = random.nextDouble() * this.getHeight();
                            double offsetZ = (random.nextDouble() - 0.5) * this.getWidth();

                            ParticleEffect particleEffect = (ParticleEffect) (random.nextDouble() < 0.3 ? POP_BUBBLE : SPARK);

                            // Добавляем частицу в мир
                            this.getWorld().addParticle(particleEffect, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, offsetX / 3, offsetY / 3, offsetZ / 3);
                        }
                    }
                }
            }
        }
    }

    private void applyBuoyancy() {
        if (followingPlayer) return;
        Vec3d velocity = this.getVelocity();

        // Переключение направления каждые 30 тиков
        if (this.age % 30 == 0) goingUp = !goingUp;

        // Рассчитываем смещение плавучести в зависимости от значения wetnessTimer
        // Чем больше значение wetnessTimer, тем меньше смещение
        double maxBuoyancy = 0.03; // Максимальное смещение плавучести, когда губка полностью сухая
        double minBuoyancy = 0.005; // Минимальное смещение плавучести, когда губка полностью мокрая
        // Смещение для контроля плавучести
        double buoyancyOffset = minBuoyancy + (maxBuoyancy - minBuoyancy) * (1 - (wetnessTimer / 6000.0)); // Линейная интерполяция

        // Установить плавучесть в зависимости от направления
        if (!goingUp) {
            buoyancyOffset = -buoyancyOffset; // Плавучесть вниз
        }

        // Применить плавучесть
        this.setVelocity(velocity.x, buoyancyOffset, velocity.z);
    }

    private void spawnWaterParticles() {
        int particlesPerTick = Math.max(1, 6000 / (wetnessTimer + 1)); // Определить количество частиц за тик

        if (this.age % (particlesPerTick + 1) == 0) {
            // Создаем частицы воды вокруг сущности
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();

            double offsetX = (this.random.nextDouble() - 0.5) * this.getWidth();
            double offsetY = this.random.nextDouble() * this.getHeight();
            double offsetZ = (this.random.nextDouble() - 0.5) * this.getWidth();

            this.getWorld().addParticle(ParticleTypes.FALLING_WATER, x + offsetX, y + offsetY, z + offsetZ, 0, 0, 0);
        }
    }

    private boolean checkForHeatSource() {
        // Радиус проверки на наличие источника тепла
        int radius = 5;
        BlockPos currentPos = this.getBlockPos();

        // Проверяем каждый блок в радиусе 5 блоков от губки
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos checkPos = currentPos.add(x, y, z);
                    Block block = this.getWorld().getBlockState(checkPos).getBlock();
                    if (block == Blocks.FIRE ||
                            block == Blocks.LAVA ||
                            block == Blocks.SOUL_FIRE ||
                            block == Blocks.CAMPFIRE ||
                            block == Blocks.SOUL_CAMPFIRE ||
                            block == Blocks.TORCH ||
                            block == Blocks.SOUL_TORCH) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            this.setInvulnerable(true);
            followingPlayer = !followingPlayer; // Переключать состояние следования за игроком
            Objects.requireNonNull(MinecraftClient.getInstance().player).playSound(this.isWet() ? SoundEvents.BLOCK_WET_SPONGE_PLACE : SoundEvents.BLOCK_SPONGE_PLACE, .75f, 1.8f);
            return ActionResult.success(this.getWorld().isClient);
        }
        return ActionResult.SUCCESS;
    }

    public boolean isWet() {
        return wetnessTimer > 0; // Возвращаем true, если губка мокрая (таймер > 0)
    }
}
