package com.example.examplemod.multiblock.validation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record LayerReference(String template, RepeatRange repeat) {

    public record RepeatRange(int min, int max) {
        public static final Codec<RepeatRange> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("min").forGetter(RepeatRange::min),
                        Codec.INT.fieldOf("max").forGetter(RepeatRange::max)
                ).apply(instance, RepeatRange::new)
        );
    }

    public static final RepeatRange DEFAULT_REPEAT = new RepeatRange(1, 1);

    public static final Codec<LayerReference> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("template").forGetter(LayerReference::template),
                    RepeatRange.CODEC.optionalFieldOf("repeat")
                            .xmap(
                                    opt -> opt.orElse(DEFAULT_REPEAT),
                                    r -> r.equals(DEFAULT_REPEAT) ? Optional.empty() : Optional.of(r)
                            )
                            .forGetter(LayerReference::repeat)
            ).apply(instance, LayerReference::new)
    );
}