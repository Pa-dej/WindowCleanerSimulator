package me.Padej_.windowcleanersimulator.entity.model;

import com.google.common.collect.ImmutableList;
import me.Padej_.windowcleanersimulator.entity.Sponge;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;

public class SpongeModel extends EntityModel<Sponge> {
    private final ModelPart base;

    public SpongeModel(ModelPart modelPart) {
        this.base = modelPart.getChild(EntityModelPartNames.CUBE);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(EntityModelPartNames.CUBE,
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setAngles(Sponge entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        ImmutableList.of(this.base).forEach((modelRenderer) -> modelRenderer.render(matrices, vertices, light, overlay, color));
    }
}
