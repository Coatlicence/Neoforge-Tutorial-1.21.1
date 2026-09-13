package com.example.examplemod.blockentities.multiblock.controller;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.blockentities.ModBlockEntities;

import com.example.examplemod.multiblock.IMultiblockController;
import com.example.examplemod.multiblock.MultiblockState;
import com.example.examplemod.multiblock.validation.MultiblockStructures;
import com.example.examplemod.multiblock.validation.MultiblockValidator;
import com.example.examplemod.multiblock.validation.data.MultiblockDefinition;
import com.example.examplemod.multiblock.validation.data.ValidationResult;
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
import org.jetbrains.annotations.NotNull;


import java.util.List;


import static com.example.examplemod.block.multiblock.controller.EnhancedBlastFurnaceOutput.FORMED;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class EnhancedBlastFurnaceOutputBlockEntity extends BlockEntity implements IHaveGoggleInformation, IMultiblockController {

    private int detectedHeight = 0;

    private final MultiblockState state = new MultiblockState();

    @Override
    public @NotNull MultiblockState getMultiblockState() {
        return java.util.Objects.requireNonNull(this.state, "MultiblockState не может быть null!");
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

        boolean wasFormed = this.state.isFormed();
        BlockPos oldFailedPos = this.state.getFailedPos();

        ValidationResult res = MultiblockValidator.validate(level, this, def);

        state.updateValidationResult(res);

        if (wasFormed != state.isFormed() || !java.util.Objects.equals(oldFailedPos, state.getFailedPos())) {
            level.setBlock(worldPosition, getBlockState().setValue(FORMED, state.isFormed()), Block.UPDATE_CLIENTS);
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
        this.state.addGoggleInformation(tooltip, isPlayerSneaking);

        if (state.isFormed()) {
            tooltip.add(Component.literal("Высота доменной печи: §6" + this.detectedHeight));
        }
        return true;
    }


    ///--------------------------------------------------------
    // DATA: Save & Load

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        state.saveAdditional(tag, registries);

        tag.putInt("Height", this.detectedHeight);

        // TODO: save inv, progress
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        state.loadAdditional(tag, registries);

        this.detectedHeight = tag.getInt("Height");

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
            state.setFormed(getBlockState().getValue(FORMED));
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