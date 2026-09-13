package com.example.examplemod.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface IMultiblockController  {

    public abstract BlockPos getPosition();

    public abstract Direction getFacing();

    public abstract void revalidateStructure();
}
