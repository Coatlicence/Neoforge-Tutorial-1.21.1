package com.example.examplemod.block.multiblock.controller;

import com.drmangotea.tfmg.base.blocks.TFMGHorizontalDirectionalBlock;
import com.example.examplemod.blockentities.multiblock.controller.EnhancedBlastFurnaceOutputBlockEntity;
import com.example.examplemod.blockentities.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;
import com.simibubi.create.foundation.block.IBE;

public class EnhancedBlastFurnaceOutput extends TFMGHorizontalDirectionalBlock implements IBE<EnhancedBlastFurnaceOutputBlockEntity> {
    public EnhancedBlastFurnaceOutput(BlockBehaviour.Properties properties) { super(properties); }

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED);
    }

    @Override
    public Class<EnhancedBlastFurnaceOutputBlockEntity> getBlockEntityClass() {
        return EnhancedBlastFurnaceOutputBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EnhancedBlastFurnaceOutputBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ENHANCED_BLAST_FURNACE.get();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EnhancedBlastFurnaceOutputBlockEntity controller) {
                controller.revalidateStructure();
            }
        }
    }
}
