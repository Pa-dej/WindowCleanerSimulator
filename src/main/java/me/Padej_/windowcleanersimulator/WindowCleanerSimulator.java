package me.Padej_.windowcleanersimulator;

import me.Padej_.windowcleanersimulator.block.RegisterBlocks;
import me.Padej_.windowcleanersimulator.entity.ModEntities;
import me.Padej_.windowcleanersimulator.entity.Sponge;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowCleanerSimulator implements ModInitializer {
    
    public static String MOD_ID = "windowcleanersimulator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ParticleType SPARK = FabricParticleTypes.simple();
    public static final ParticleType POP_BUBBLE = FabricParticleTypes.simple();

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(ModEntities.SPONGE, Sponge.createMobAttributes());

        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "spark"), SPARK);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "pop_bubble"), POP_BUBBLE);

        RegisterBlocks.initialize();
    }
}
