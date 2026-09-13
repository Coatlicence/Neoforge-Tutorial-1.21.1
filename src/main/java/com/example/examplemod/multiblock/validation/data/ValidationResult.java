package com.example.examplemod.multiblock.validation.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public record ValidationResult(
        boolean isValid,
        @Nullable BlockPos failedPos,      // Где именно не хватает блока
        @Nullable Block expectedBlock,      // Какой блок ожидался
        @Nullable String errorMessage
) {
    // Удобный синглтон для успешной проверки
    public static final ValidationResult SUCCESS =
            new ValidationResult(true, null, null, null);
}