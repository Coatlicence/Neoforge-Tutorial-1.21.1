package com.example.examplemod.multiblock.validation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record MultiblockDefinition(
        ResourceLocation id,
        Map<String, ResourceLocation> palette,
        List<LayerReference> layers,
        Map<String, LayerTemplate> templates
) {

    public static final Codec<MultiblockDefinition> CODEC = RecordCodecBuilder.<MultiblockDefinition>create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(d -> d.id),
                    Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
                            .optionalFieldOf("palette", Map.of())
                            .forGetter(d -> d.palette),
                    LayerReference.CODEC.listOf().fieldOf("layers").forGetter(d -> d.layers),
                    Codec.unboundedMap(Codec.STRING, LayerTemplate.CODEC).fieldOf("templates").forGetter(d -> d.templates)
            ).apply(instance, MultiblockDefinition::new)
    ).validate(MultiblockDefinition::crossValidate);

    private static DataResult<MultiblockDefinition> crossValidate(MultiblockDefinition def) {

        // 1. Проверка: все ли шаблоны из layers существуют в templates
        for (int i = 0; i < def.layers().size(); i++) {
            String templateName = def.layers().get(i).template();
            if (!def.templates().containsKey(templateName)) {
                final int layerIndex = i;
                return DataResult.error(() ->
                        "layers[" + layerIndex + "] references unknown template '" + templateName + "'");
            }
        }

        // 2. Проверка: все ли значения палитры существуют в реестре блоков
        for (Map.Entry<String, ResourceLocation> entry : def.palette().entrySet()) {
            if (!BuiltInRegistries.BLOCK.containsKey(entry.getValue())) {
                return DataResult.error(() ->
                        "Palette key '" + entry.getKey() + "' references unknown block: " + entry.getValue());
            }
        }

        // 3. Проверка: все ли ключи base в ячейках существуют в палитре
        for (Map.Entry<String, LayerTemplate> templateEntry : def.templates().entrySet()) {
            String templateName = templateEntry.getKey();
            LayerTemplate template = templateEntry.getValue();

            for (int i = 0; i < template.cells().size(); i++) {
                Cell cell = template.cells().get(i);
                if (!def.palette().containsKey(cell.base())) {
                    final int cellIndex = i;
                    return DataResult.error(() ->
                            "Template '" + templateName + "' cell[" + cellIndex +
                                    "] references unknown palette key: '" + cell.base() + "'");
                }
            }
        }

        // 4. Проверка: запрет семантического равенства соседних слоёв
        for (int i = 0; i < def.layers().size() - 1; i++) {
            LayerTemplate current = def.templates().get(def.layers().get(i).template());
            LayerTemplate next = def.templates().get(def.layers().get(i + 1).template());

            if (areLayersSemanticallyEqual(current, next)) {
                final int idx = i;
                return DataResult.error(() ->
                        "layers[" + idx + "] and layers[" + (idx + 1) +
                                "] are semantically identical. Merge them or differentiate.");
            }
        }

        return DataResult.success(def);
    }

    private static boolean areLayersSemanticallyEqual(LayerTemplate a, LayerTemplate b) {
        if (a.cells().size() != b.cells().size()) return false;
        for (int i = 0; i < a.cells().size(); i++) {
            Cell cellA = a.cells().get(i);
            Cell cellB = b.cells().get(i);
            if (cellA.offset()[0] != cellB.offset()[0]) return false;
            if (cellA.offset()[1] != cellB.offset()[1]) return false;
            if (!cellA.base().equals(cellB.base())) return false;
        }
        return true;
    }

    // Удобный метод: получить Block по ключу палитры
    public net.minecraft.world.level.block.Block resolveBlock(String paletteKey) {
        ResourceLocation loc = palette.get(paletteKey);
        if (loc == null) return null;
        return BuiltInRegistries.BLOCK.get(loc);
    }
}