package com.example.examplemod.multiblock.validation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public record Cell(@Nullable String id, int[] offset, Block base) {

    public static final Codec<Cell> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("id").forGetter(c -> Optional.ofNullable(c.id)),
                    Codec.INT.listOf().comapFlatMap(
                            list -> {
                                if (list.size() != 2) return DataResult.error(() -> "offset must have 2 elements");
                                return DataResult.success(new int[]{list.get(0), list.get(1)});
                            },
                            arr -> List.of(arr[0], arr[1])
                    ).fieldOf("offset").forGetter(Cell::offset),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("base").forGetter(c -> c.base)
            ).apply(instance, (id, offset, base) -> new Cell(id.orElse(null), offset, base))
    );
}
