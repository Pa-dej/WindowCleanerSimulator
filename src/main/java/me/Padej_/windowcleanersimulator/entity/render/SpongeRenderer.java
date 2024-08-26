package me.Padej_.windowcleanersimulator.entity.render;

import me.Padej_.windowcleanersimulator.client.WindowCleanerSimulatorClient;
import me.Padej_.windowcleanersimulator.entity.Sponge;
import me.Padej_.windowcleanersimulator.entity.model.SpongeModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

import static me.Padej_.windowcleanersimulator.WindowCleanerSimulator.MOD_ID;

public class SpongeRenderer extends MobEntityRenderer<Sponge, SpongeModel> {

    public SpongeRenderer(EntityRendererFactory.Context context) {
        super(context, new SpongeModel(context.getPart(WindowCleanerSimulatorClient.SPONGE_LAYER)), 0.25f);
    }

    @Override
    public Identifier getTexture(Sponge entity) {
        return Sponge.wetnessTimer == 0 ? Identifier.of(MOD_ID, "textures/entity/sponge/sponge.png") : Identifier.of(MOD_ID, "textures/entity/sponge/wet_sponge.png");
    }
}
