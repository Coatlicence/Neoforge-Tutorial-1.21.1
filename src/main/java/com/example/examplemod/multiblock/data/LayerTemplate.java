package com.example.examplemod.multiblock.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.DataResult;

import java.util.List;

public record LayerTemplate(List<Cell> cells) {

    public static final Codec<LayerTemplate> CODEC = RecordCodecBuilder.<LayerTemplate>create(instance ->
            instance.group(
                    Cell.CODEC.listOf().fieldOf("cells").forGetter(t -> t.cells)
            ).apply(instance, LayerTemplate::new)
    ).validate(template ->
            template.cells().isEmpty()
                    ? DataResult.error(() -> "Layer template has no cells")
                    : DataResult.success(template)
    );
}