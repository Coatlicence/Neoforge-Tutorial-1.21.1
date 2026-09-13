package com.example.examplemod.multiblock;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;

public class MultiblockNotifier {

    private static Map<Block, AABB> AABBMap = new HashMap<>();

    public static void registerAABBs(Map<Block, AABB> aabbMap) {
        AABBMap.clear();

        AABBMap.putAll(aabbMap);
    }

    public static void notify(Level level, BlockPos blockPos, Block block) {
        if (level.isClientSide()) return;

        ExampleMod.LOGGER.info("Событие уведомления вызвал {} v {}", block, blockPos);

        AABB aabb = AABBMap.get(block);
        if (aabb == null) return;

        ExampleMod.LOGGER.info("Найден соответствующий ААББ {}", aabb);


        // Перебираем все возможные смещения контроллера относительно блока.
        // Локальный AABB: "блок = контроллер + offset". Значит "контроллер = блок - offset".
        int minX = (int) Math.floor(aabb.minX);
        int maxX = (int) Math.floor(aabb.maxX);
        int minY = (int) Math.floor(aabb.minY);
        int maxY = (int) Math.floor(aabb.maxY);
        int minZ = (int) Math.floor(aabb.minZ);
        int maxZ = (int) Math.floor(aabb.maxZ);

        for (int dx = minX; dx <= maxX; dx++) {
            for (int dy = minY; dy <= maxY; dy++) {
                for (int dz = minZ; dz <= maxZ; dz++) {

                    BlockPos controllerPos = blockPos.offset(-dx, -dy, -dz);

                    if (!level.isLoaded(controllerPos)) continue;

                    BlockEntity be = level.getBlockEntity(controllerPos);
                    if (!(be instanceof IMultiblockController controller)) continue;
                    if (be.isRemoved()) continue;

                    controller.revalidateStructure();
                }
            }
        }
    }

}
