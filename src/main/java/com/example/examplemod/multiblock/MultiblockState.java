package com.example.examplemod.multiblock;

import com.example.examplemod.multiblock.validation.data.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiblockState {

    private boolean isFormed = false;

    @Nullable
    private BlockPos failedPos = null;

    @Nullable
    private Block expectedBlock = null;

    @Nullable
    private String errorMessage = null;

    public boolean isFormed() { return isFormed; }
    public void setFormed(boolean formed) { this.isFormed = formed; }

    public BlockPos getFailedPos() { return failedPos; }
    public void setFailedPos(@Nullable BlockPos failedPos)
        { this.failedPos = failedPos; }

    public Block getExpectedBlock() { return expectedBlock; }
    public void setExpectedBlock(@Nullable Block expectedBlock)
        { this.expectedBlock = expectedBlock; }

    public String getErrorMessage() {return errorMessage; }
    private void setErrorMessage(@Nullable String errorMessage)
        { this.errorMessage = errorMessage; }

    // Удобный метод для сброса/обновления за один раз
    public void updateValidationResult(ValidationResult result) {
        this.isFormed = result.isValid();
        this.failedPos = result.failedPos();
        this.expectedBlock = result.expectedBlock();
        this.errorMessage = result.errorMessage();
    }

    public void addGoggleInformation(List<Component> tooltip, boolean isPlayerSneaking) {

        if (this.isFormed) {
            tooltip.add(Component.literal("Статус: §aСобрана"));
        } else {
            tooltip.add(Component.literal("Статус: §cРазрушена"));

            if (this.failedPos != null) {
                tooltip.add(Component.literal("§7Позиция ошибки: §f" + this.failedPos.toShortString()));
            }

            if (this.expectedBlock != null) {
                ResourceLocation key = BuiltInRegistries.BLOCK.getKey(this.expectedBlock);
                tooltip.add(Component.literal("§7Ожидаемый блок: §f" + key));
            }

            if (this.errorMessage != null) {
                // Форматируем сообщение об ошибке для лучшей читаемости
                tooltip.add(Component.literal("§c⚠ " + this.errorMessage));
            } else {
                tooltip.add(Component.literal("§7Причина: §cНеизвестная ошибка структуры"));
            }
        }
    }

    ///--------------------------------------------------------
    // DATA: Save & Load

    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        tag.putBoolean("Formed", this.isFormed);

        if (this.failedPos != null) {
            tag.putLong("FailedPos", this.failedPos.asLong());
        } else {
            tag.remove("FailedPos");
        }

        if (this.expectedBlock != null) {
            // Получаем уникальный ID блока (например, "examplemod:enhanced_hatch_block")
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(this.expectedBlock);
            tag.putString("expectedBlock", key.toString());
        }

        if (this.errorMessage != null) {
            tag.putString("errorMessage", errorMessage);
        }
        else {
            tag.remove("errorMessage");
        }
    }

    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        this.isFormed = tag.getBoolean("Formed");

        if (tag.contains("FailedPos")) {
            this.failedPos = BlockPos.of(tag.getLong("FailedPos"));
        } else {
            this.failedPos = null;
        }

        if (tag.contains("expectedBlock")) {
            // Читаем строку и пытаемся преобразовать её обратно в ResourceLocation
            String blockId = tag.getString("expectedBlock");
            ResourceLocation key = ResourceLocation.tryParse(blockId);

            // Проверяем, что строка была валидной и блок всё ещё существует в реестре
            if (key != null && BuiltInRegistries.BLOCK.containsKey(key)) {
                this.expectedBlock = BuiltInRegistries.BLOCK.get(key);
            } else {
                this.expectedBlock = null; // Блок был удалён из игры/датапака
            }
        } else {
            this.expectedBlock = null;
        }

        if (tag.contains("errorMessage")) {
            this.errorMessage = tag.getString("errorMessage");
        } else {
            this.errorMessage = null;
        }
    }
}
