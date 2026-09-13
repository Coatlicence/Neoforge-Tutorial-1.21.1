package com.example.examplemod.multiblock.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public record ValidationResult(
        boolean isValid,
        @Nullable BlockPos failedPos,      // Где именно не хватает блока
        @Nullable Block expectedBlock      // Какой блок ожидался
) {
    // Удобный синглтон для успешной проверки
    public static final ValidationResult SUCCESS = new ValidationResult(true, null, null);
}