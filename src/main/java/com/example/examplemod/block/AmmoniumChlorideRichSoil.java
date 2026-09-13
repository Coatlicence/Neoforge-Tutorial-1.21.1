package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
//import farmersdelight.common.block.RichSoilBlock; // Импортируем оригинальный класс

import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.RichSoilBlock;

public class AmmoniumChlorideRichSoil extends RichSoilBlock
{
    public AmmoniumChlorideRichSoil(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility toolAction, boolean simulate) {
        return toolAction.equals(
                ItemAbilities.HOE_TILL) &&
                context.getLevel().getBlockState(context.getClickedPos().above()).isAir()
                    ? ((Block) ModBlocks.AMMONIUM_CHLORIDE_RICH_SOIL_FARMLAND.get()).defaultBlockState()
                    : null;
    }
}
