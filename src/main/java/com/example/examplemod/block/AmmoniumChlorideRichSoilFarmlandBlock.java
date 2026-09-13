package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.common.CommonHooks;
// Убираем зависимости от FD, если они могут не быть
// import vectorwing.farmersdelight.common.Configuration;
// import vectorwing.farmersdelight.common.utility.MathUtils;
import com.example.examplemod.tag.ModBlockTags;

public class AmmoniumChlorideRichSoilFarmlandBlock extends FarmBlock {

    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;

    public AmmoniumChlorideRichSoilFarmlandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(MOISTURE, 7));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            }
        } else if (moisture < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2); // <-- Пашня чернеет
        } else if (moisture == 7) {
            // Используем константу, чтобы избежать проблем с конфигом FD
            float boostChance = 0.1F; // 10% шанс ускорения, можно вынести в конфиг
            if (boostChance == 0.0F) {
                return;
            }

            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            if (!aboveState.is(ModBlockTags.AFFECTED_BY_CHLORINE_SOIL)) {
                return; // Проверка тега
            }

            // Проверяем тег из FD - убедись, что FD установлен
            if (aboveState.is(vectorwing.farmersdelight.common.tag.ModTags.UNAFFECTED_BY_RICH_SOIL) ||
                    aboveState.getBlock() instanceof net.minecraft.world.level.block.TallFlowerBlock) {
                return;
            }

            if (aboveState.getBlock() instanceof BonemealableBlock) {
                BonemealableBlock growable = (BonemealableBlock) aboveState.getBlock();
                // Заменяем MathUtils.RAND.nextFloat() на random.nextFloat()
                if (random.nextFloat() <= boostChance && // <-- Вот тут
                        growable.isValidBonemealTarget(level, abovePos, aboveState) &&
                        CommonHooks.canCropGrow(level, abovePos, aboveState, true)) {

                    growable.performBonemeal(level, level.random, abovePos, aboveState);
                    CommonHooks.fireCropGrowPost(level, abovePos, aboveState);
                }
            }
        }
    }

    public static void turnToRichSoil(net.minecraft.world.entity.Entity entity, net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, pushEntitiesUp(state, com.example.examplemod.block.ModBlocks.AMMONIUM_CHLORIDE_RICH_SOIL.get().defaultBlockState(), level, pos));
        level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, pos, net.minecraft.world.level.gameevent.GameEvent.Context.of(entity, state));
    }

    @Override
    public void tick(net.minecraft.world.level.block.state.BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (!state.canSurvive(level, pos)) {
            turnToRichSoil(null, state, level, pos);
        }
    }

    private static boolean isNearWater(LevelReader level, BlockPos pos) {
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        for(BlockPos nearbyPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (state.canBeHydrated(level, pos, level.getFluidState(nearbyPos), nearbyPos)) {
                return true;
            }
        }
        return net.neoforged.neoforge.common.FarmlandWaterManager.hasBlockWaterTicket(level, pos);
    }
}