package me.Padej_.windowcleanersimulator.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static me.Padej_.windowcleanersimulator.WindowCleanerSimulator.MOD_ID;

public class ModEntities {
    public static final EntityType<Sponge> SPONGE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "sponge"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, Sponge::new).dimensions(EntityDimensions.fixed(0.375f, 0.375f)).build()
    );
}
