package com.example.examplemod.multiblock.validation;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.multiblock.IMultiblockController;
import com.example.examplemod.multiblock.validation.data.Cell;
import com.example.examplemod.multiblock.validation.data.LayerTemplate;
import com.example.examplemod.multiblock.validation.data.MultiblockDefinition;
import com.example.examplemod.multiblock.validation.data.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public class MultiblockValidator {

    public static int[] transformOffset(int[] localOffset, Direction facing) {

        int lx = localOffset[0];
        int lz = localOffset[1];

        switch (facing) {
            case NORTH: // Контроллер смотрит на север
                // Локальная система совпадает с мировой
                return new int[]{lx, lz};

            case EAST: // Контроллер смотрит на восток (поворот на 90° по часовой)
                return new int[]{-lz, lx};

            case SOUTH: // Контроллер смотрит на юг (поворот на 180°)
                // Обе оси инвертируются
                return new int[]{-lx, -lz};

            case WEST: // Контроллер смотрит на запад (поворот на 270° по часовой)
                // Локальный X становится мировым -Z, локальный Z становится мировым X
                return new int[]{lz, -lx};


            default:
                return new int[]{lx, lz};
        }
    }

    public static ValidationResult validate(Level level, IMultiblockController controller, MultiblockDefinition def) {

        if (level == null || level.isClientSide)
            return new ValidationResult(false, null, null, null);

        if (controller == null)
            return new ValidationResult(false, null, null, null); // вернуть ошибку требуется контроллер

        BlockPos controllerPos = controller.getPosition();

        if (def == null)
            return new ValidationResult(false, controllerPos, null, "Контроллер не имеет JSON-определения структуры\n MultiblockDefinition равен null"); // Или кинуть исключение, это баг конфигурации

        int currentWorldY = 0;

        for (int i = 0; i < def.layers().size(); i++) {
            var currentLayer = def.layers().get(i);

            var currentLayerRepeat = currentLayer.repeat();
            int max = currentLayerRepeat.max();
            int min = currentLayerRepeat.min();

            var currentLayerTemplate = def.templates().get(currentLayer.template());

            ExampleMod.LOGGER.info("for layer {} min {} max {}", currentLayerTemplate, min, max);

            int currentLayerRepeatCount = 0;

            while (currentLayerRepeatCount < max) {

                var check = checkLayer(level, controllerPos, controller.getFacing(), currentLayerTemplate, currentWorldY, def);

                if (check.matched()) {
                    currentLayerRepeatCount++;
                    currentWorldY++;
                }
                else if (currentLayerRepeatCount < min) {
                    String msg = String.format("Слой '%s' ожидался минимум %d раз, получено %d",
                            currentLayer.template(), min, currentLayerRepeatCount);

                    return new ValidationResult(false, check.failPos(), check.failExpected(), msg);
                }
                else {
                    break; // Достигли минимума, но паттерн сменился. Это легитимная остановка
                }
            } // while layerCount < max ends here

        }

        return ValidationResult.SUCCESS;
    }
    // end of validate

    // === Вспомогательный метод проверки одного среза (Y-уровня) ===
    private static LayerCheckResult checkLayer(Level level, BlockPos controllerPos, Direction facing, LayerTemplate template, int worldY, MultiblockDefinition def) {
        for (Cell cell : template.cells()) {
            int[] transformed = transformOffset(cell.offset(), facing);
            BlockPos pos = controllerPos.offset(transformed[0], worldY, transformed[1]);

            BlockState stateInWorld = level.getBlockState(pos);
            Block expectedBlock = def.resolveBlock(cell.base());

            if (expectedBlock == null) {
                // Этого не должно произойти благодаря crossValidate, но для безопасности
                return new LayerCheckResult(false, pos, null);
            }

            if (!stateInWorld.is(expectedBlock)) {
                return new LayerCheckResult(false, pos, expectedBlock);
            }
        }
        return new LayerCheckResult(true, null, null);
    }

    // Вспомогательный рекорд для чистого возврата из checkLayer
    private record LayerCheckResult(boolean matched, BlockPos failPos, Block failExpected) {}
}
