package com.example.examplemod.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMultiblockController  {

    @NotNull
    public abstract MultiblockState getMultiblockState();

    public abstract BlockPos getPosition();

    public abstract Direction getFacing();

    public abstract void revalidateStructure();

}
