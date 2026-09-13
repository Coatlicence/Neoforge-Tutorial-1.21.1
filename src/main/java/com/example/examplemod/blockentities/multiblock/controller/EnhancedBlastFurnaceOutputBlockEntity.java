package com.example.examplemod.blockentities.multiblock.controller;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.blockentities.ModBlockEntities;

import com.example.examplemod.multiblock.IMultiblockController;
import com.example.examplemod.multiblock.MultiblockStructures;
import com.example.examplemod.multiblock.MultiblockValidator;
import com.example.examplemod.multiblock.data.MultiblockDefinition;
import com.example.examplemod.multiblock.data.ValidationResult;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


import java.util.List;


import static com.example.examplemod.block.multiblock.controller.EnhancedBlastFurnaceOutput.FORMED;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class EnhancedBlastFurnaceOutputBlockEntity extends BlockEntity implements IHaveGoggleInformation, IMultiblockController {

    private boolean isFormed = false;
    private int detectedHeight = 0;

    // Позиция блока, который нарушает структуру (null если всё ОК)
    private BlockPos failedPos = null;

    // Геттер для рендерера
    public BlockPos getFailedPos() {
        return this.failedPos;
    }

    public EnhancedBlastFurnaceOutputBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ENHANCED_BLAST_FURNACE.get(), pos, blockState);
    }

    ///--------------------------------------------------
    // STRUCTURE: Validation & Registration

    public static void onStructureChanged(Level level, BlockPos controllerPos) {
        BlockEntity be = level.getBlockEntity(controllerPos);
        if (be instanceof EnhancedBlastFurnaceOutputBlockEntity controller) {
            controller.revalidateStructure();
            ExampleMod.LOGGER.info("Callback вызван: controllerPos={}", controllerPos);
        }
    }

    @Override
    public void revalidateStructure() {

        if (level == null || level.isClientSide) {
            return;
        }

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("examplemod", "test_column_1");
        MultiblockDefinition def = MultiblockStructures.get(id);

        boolean wasFormed = this.isFormed;
        BlockPos oldFailedPos = this.failedPos;

        ValidationResult res = MultiblockValidator.validate(level, this, def);

        this.isFormed = res.isValid();
        this.failedPos = res.isValid() ? null : res.failedPos(); // Запоминаем ошибку

        if (wasFormed != this.isFormed || !java.util.Objects.equals(oldFailedPos, this.failedPos)) {
            level.setBlock(worldPosition, getBlockState().setValue(FORMED, this.isFormed), Block.UPDATE_CLIENTS);
            setChanged();

            // ВАЖНО: Принудительно синхронизируем данные с клиентом,
            // чтобы он сразу узнал о failedPos без перезагрузки чанка
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }

        // end of validation
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     §6Усиленная доменная печь"));
        boolean formed = this.getBlockState().getValue(FORMED);
        if (formed) {
            tooltip.add(Component.literal("Статус: §aСобрана"));
        } else {
            tooltip.add(Component.literal("Статус: §cРазрушена"));
            if (this.failedPos != null) {
                tooltip.add(Component.literal("§7Ошибка в блоке: §f" + this.failedPos.toShortString()));
            }
        }
        return true;
    }


    ///--------------------------------------------------------
    // DATA: Save & Load

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("Formed", this.isFormed);
        tag.putInt("Height", this.detectedHeight);

        if (this.failedPos != null) {
            tag.putLong("FailedPos", this.failedPos.asLong());
        } else {
            // Явно удаляем, если было, но стало null
            tag.remove("FailedPos");
        }
        // TODO: save inv, progress
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.isFormed = tag.getBoolean("Formed");
        this.detectedHeight = tag.getInt("Height");

        if (tag.contains("FailedPos")) {
            this.failedPos = BlockPos.of(tag.getLong("FailedPos"));
        } else {
            this.failedPos = null;
        }
        // TODO: load inv, progress
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {

        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {

        loadAdditional(tag, lookupProvider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null && !level.isClientSide) {
            isFormed = getBlockState().getValue(FORMED);
            // Перепроверка при загрузке чанка на сервере
            revalidateStructure();
        }
    }

    @Override
    public BlockPos getPosition() {
        return getBlockPos();
    }

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(FACING);
    }
}