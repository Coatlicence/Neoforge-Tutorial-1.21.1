package com.example.examplemod.multiblock.validation;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.multiblock.validation.data.Cell;
import com.example.examplemod.multiblock.validation.data.LayerReference;
import com.example.examplemod.multiblock.validation.data.LayerTemplate;
import com.example.examplemod.multiblock.validation.data.MultiblockDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MultiblockStructures {

    private static Map<ResourceLocation, MultiblockDefinition> CACHE = Map.of();

    public static MultiblockDefinition get(ResourceLocation id) {
        return CACHE.get(id);
    }

    public static void replace(Map<ResourceLocation, MultiblockDefinition> newCache) {
        CACHE = Map.copyOf(newCache);
    }

    public static Map<Block, AABB> buildBlockAABBMap() {
        Map<Block, AABB> result = new HashMap<>();

        for (MultiblockDefinition def : CACHE.values()) {
            AABB localAABB = computeLocalAABB(def);

            Set<Block> uniqueBlocks = new HashSet<>();
            for (LayerReference layerRef : def.layers()) {
                LayerTemplate template = def.templates().get(layerRef.template());
                for (Cell cell : template.cells()) {
                    // Резолвим ключ палитры в Block
                    Block block = def.resolveBlock(cell.base());
                    if (block != null) {
                        uniqueBlocks.add(block);
                    }
                }
            }

            for (Block block : uniqueBlocks) {
                result.merge(block, localAABB, MultiblockStructures::unionAABB);
            }
        }

        return result;
    }

    private static AABB computeLocalAABB(MultiblockDefinition def) {
        int maxWidth = 0;
        int maxHeight = 0;

        for (LayerReference layerRef : def.layers()) {
            LayerTemplate template = def.templates().get(layerRef.template());
            int repeatMax = layerRef.repeat().max();

            // Вычисляем ширину по всем ячейкам этого слоя
            for (Cell cell : template.cells()) {
                int lx = Math.abs(cell.offset()[0]);
                int lz = Math.abs(cell.offset()[1]);
                maxWidth = Math.max(maxWidth, Math.max(lx, lz));
            }

            // Высота увеличивается на количество повторений
            maxHeight += repeatMax;
        }


        ExampleMod.LOGGER.info("DEF: {}, AABB {} {}", def.id(), maxWidth, maxHeight);
        // Симметричный AABB: от -maxWidth до +maxWidth по X и Z, от 0 до maxHeight по Y
        return new AABB(-maxWidth, 0, -maxWidth, maxWidth, maxHeight, maxWidth);
    }

    private static AABB unionAABB(AABB a, AABB b) {
        return new AABB(
                Math.min(a.minX, b.minX),
                Math.min(a.minY, b.minY),
                Math.min(a.minZ, b.minZ),
                Math.max(a.maxX, b.maxX),
                Math.max(a.maxY, b.maxY),
                Math.max(a.maxZ, b.maxZ)
        );
    }
}
