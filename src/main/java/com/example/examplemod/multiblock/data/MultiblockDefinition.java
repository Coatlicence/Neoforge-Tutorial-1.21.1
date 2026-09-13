package com.example.examplemod.multiblock.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record MultiblockDefinition(
        ResourceLocation id,
        List<LayerReference> layers,
        Map<String, LayerTemplate> templates
) {

    public static final Codec<MultiblockDefinition> CODEC = RecordCodecBuilder.<MultiblockDefinition>create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(d -> d.id),
                    LayerReference.CODEC.listOf().fieldOf("layers").forGetter(d -> d.layers),
                    Codec.unboundedMap(Codec.STRING, LayerTemplate.CODEC).fieldOf("templates").forGetter(d -> d.templates)
            ).apply(instance, MultiblockDefinition::new)
    ).validate(MultiblockDefinition::crossValidate);

    private static DataResult<MultiblockDefinition> crossValidate(MultiblockDefinition def) {
        for (int i = 0; i < def.layers().size(); i++) {
            String templateName = def.layers().get(i).template();
            if (!def.templates().containsKey(templateName)) {
                final int layerIndex = i;
                return DataResult.error(() ->
                        "layers[" + layerIndex + "] references unknown template '" + templateName + "'");
            }
        }
        return DataResult.success(def);
    }
}