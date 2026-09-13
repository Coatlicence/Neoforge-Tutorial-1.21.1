package com.example.examplemod.multiblock;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.multiblock.data.Cell;
import com.example.examplemod.multiblock.data.LayerTemplate;
import com.example.examplemod.multiblock.data.MultiblockDefinition;
import com.example.examplemod.multiblock.data.ValidationResult;
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
            return new ValidationResult(false, null, null);

        if (controller == null)
            return new ValidationResult(false, null, null); // вернуть ошибку требуется контроллер

        BlockPos controllerPos = controller.getPosition();

        if (def == null)
            return new ValidationResult(false, controllerPos, null); // Или кинуть исключение, это баг конфигурации

        int currentWorldY = 0;

        ExampleMod.LOGGER.info("--------------------------------------------------------");
        ExampleMod.LOGGER.info("--------------------------------------------------------");
        ExampleMod.LOGGER.info("--------------------------------------------------------");
        ExampleMod.LOGGER.info("Start validation of controller at pos {}", controllerPos);
        for (int i = 0; i < def.layers().size(); i++) {
            var currentLayer = def.layers().get(i);

            var currentLayerRepeat = currentLayer.repeat();
            int max = currentLayerRepeat.max();
            int min = currentLayerRepeat.min();

            var currentLayerTemplate = def.templates().get(currentLayer.template());

            ExampleMod.LOGGER.info("for layer {} min {} max {}", currentLayerTemplate, min, max);

            int currentLayerRepeatCount = 0;

            while (currentLayerRepeatCount < max) {

//                var cells = currentLayerTemplate.cells();
//
//                for (Cell current_cell : cells) {
//                    var offset = current_cell.offset();
//
//                    var transformedOffset = transformOffset(offset, controller.getFacing());
//
//                    BlockPos pos = controllerPos.offset(transformedOffset[0], i, transformedOffset[1]);
//
//                    ExampleMod.LOGGER.info("get BlockPos {} {} {}", transformedOffset[0], i, transformedOffset[1]);
//
//                    var blockGet = current_cell.base();
//
//                    ExampleMod.LOGGER.info("get this block from cell: {}", blockGet);
//
//                    var blockInWorld = level.getBlockState(pos);
//
//                    ExampleMod.LOGGER.info("blockInWorld: {}", blockInWorld);
//
//                    boolean matches = blockInWorld.is(blockGet);
//
//                    ExampleMod.LOGGER.info("matches: {}", matches);
//
//                    if (!matches) {
//                        ExampleMod.LOGGER.info("no match at pos: {}", pos);
//
//                        break;
//                        //return new ValidationResult(false, pos, blockGet);
//                    }
//
//                    currentLayerRepeatCount++;
//
//                } // matching for one layer

                var check = checkLayer(level, controllerPos, controller.getFacing(), currentLayerTemplate, currentWorldY);

                ExampleMod.LOGGER.info("for layer {}", currentLayerTemplate);
                ExampleMod.LOGGER.info("RepeatCount: {}, currentWorldY: {}", currentLayerRepeatCount, currentWorldY);
                if (check.matched()) {
                    currentLayerRepeatCount++;
                    currentWorldY++;

                    ExampleMod.LOGGER.info("matched");
                }
                else if (currentLayerRepeatCount < min) {

                    ExampleMod.LOGGER.info("currentLayerRepeatCount < min = {}", currentLayer.repeat().min());
                    ExampleMod.LOGGER.info("blockExpected: {} block pos: {}", check.failExpected(), check.failPos());

                    return new ValidationResult(false, check.failPos(), check.failExpected());
                }
                else {
                    ExampleMod.LOGGER.info("break");
                    ExampleMod.LOGGER.info("blockExpected: {} block pos: {}", check.failExpected(), check.failPos());
                    break; // Достигли минимума, но паттерн сменился. Это легитимная остановка
                }
            } // while layerCount < max ends here

        }

        return ValidationResult.SUCCESS;
    }
    // end of validate

    // === Вспомогательный метод проверки одного среза (Y-уровня) ===
    private static LayerCheckResult checkLayer(Level level, BlockPos controllerPos, Direction facing, LayerTemplate template, int worldY) {
        for (Cell cell : template.cells()) {
            int[] transformed = transformOffset(cell.offset(), facing);
            BlockPos pos = controllerPos.offset(transformed[0], worldY, transformed[1]);

            BlockState stateInWorld = level.getBlockState(pos);
            Block expectedBlock = cell.base(); // Предполагаем, что base() возвращает Block или совместимый тип

            ExampleMod.LOGGER.info("Checking pos {} | Expected: {} | Found: {}", pos, expectedBlock, stateInWorld.getBlock());

            if (!stateInWorld.is(expectedBlock)) {
                return new LayerCheckResult(false, pos, expectedBlock);
            }
        }
        return new LayerCheckResult(true, null, null);
    }

    // Вспомогательный рекорд для чистого возврата из checkLayer
    private record LayerCheckResult(boolean matched, BlockPos failPos, Block failExpected) {}
}
