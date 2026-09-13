package com.example.examplemod.blockentities;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.blockentities.multiblock.component.EnhancedHatchBlockEntity;
import com.example.examplemod.blockentities.multiblock.controller.EnhancedBlastFurnaceOutputBlockEntity;
import com.mojang.datafixers.DSL;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExampleMod.MODID);

    public static void register(IEventBus bus) { BLOCK_ENTITIES.register(bus); }


    public static final Supplier<BlockEntityType<EnhancedBlastFurnaceOutputBlockEntity>> ENHANCED_BLAST_FURNACE =
            BLOCK_ENTITIES.register("enhanced_blast_furnace",
                    () -> BlockEntityType.Builder.of(
                            EnhancedBlastFurnaceOutputBlockEntity::new,
                            ModBlocks.ENHANCED_BLAST_FURNACE_OUTPUT.get()
                    ).build(DSL.remainderType())
            );

    public static final Supplier<BlockEntityType<EnhancedHatchBlockEntity>> ENHANCED_HATCH =
        BLOCK_ENTITIES.register("enhanced_hatch",
            () -> BlockEntityType.Builder.of(
                EnhancedHatchBlockEntity::new,
                ModBlocks.ENHANCED_HATCH_BLOCK.get()
            ).build(DSL.remainderType())
        );


}
