package com.example.examplemod.block.multiblock.component;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.blockentities.multiblock.controller.EnhancedBlastFurnaceOutputBlockEntity;
import com.example.examplemod.multiblock.MultiblockNotifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnhancedFireproofBricks extends Block {

    // Не работает из-за Синглтона и Легковеса.
    // Майнкрафт хранит только 1 тип на всю игру.
    //List<EnhancedBlastFurnaceOutputBlockEntity> MyControllers;

    public EnhancedFireproofBricks(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    private void notifyAdjacentControllers(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);

            if (be instanceof EnhancedBlastFurnaceOutputBlockEntity controller) {
                controller.revalidateStructure();
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide())
            return;

        //notifyAdjacentControllers(level, pos);
        MultiblockNotifier.notify(level, pos, this);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.isClientSide())
                return;

            MultiblockNotifier.notify(level, pos, this);


        }
        //ExampleMod.LOGGER.info("Кирпич событие: pos={}, block={}", pos, this);
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
