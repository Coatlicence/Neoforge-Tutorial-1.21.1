package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.multiblock.component.EnhancedFireproofBricks;
import com.example.examplemod.block.multiblock.component.EnhancedHatchBlock;
import com.example.examplemod.block.multiblock.controller.EnhancedBlastFurnaceOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.block.RichSoilBlock;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS
            = DeferredRegister.createBlocks(ExampleMod.MODID);


    public static final Supplier<Block> AMMONIUM_CHLORIDE_RICH_SOIL =
        BLOCKS.register("ammonium_chloride_rich_soil",
            () -> new AmmoniumChlorideRichSoil(
                    RichSoilBlock.Properties.of()));

    public static final Supplier<Block> AMMONIUM_CHLORIDE_RICH_SOIL_FARMLAND =
            BLOCKS.register("ammonium_chloride_rich_soil_farmland",
                    () -> new AmmoniumChlorideRichSoilFarmlandBlock(
                            Block.Properties.of()
                                    .strength(0.6F)
                                    .isValidSpawn((state, level, pos, entityType) -> false)
                                    .isRedstoneConductor((state, level, pos) -> false)
                                    .isSuffocating((state, level, pos) -> false)
                                    .isViewBlocking((state, level, pos) -> false)
                    )
            );


    public static final Supplier<Block> ENHANCED_BLAST_FURNACE_OUTPUT =
        BLOCKS.register("enhanced_blast_furnace_output",
            () -> new EnhancedBlastFurnaceOutput(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(2.0F, 6.0F)));

    public static final Supplier<Block> ENHANCED_FIREPROOF_BRICKS =
            BLOCKS.register("enhanced_fireproof_bricks",
                    () -> new EnhancedFireproofBricks(
                            BlockBehaviour.Properties.of()
                                    .strength(2.0F, 6.0F) // Прочность и сопротивление взрывам (как у камня/кирпича)
                                    .sound(SoundType.NETHER_BRICKS) // Звуки шагов и разрушения
                                    .requiresCorrectToolForDrops() // Требует кирки для выпадения
                                    .instrument(NoteBlockInstrument.BASEDRUM) // Звук нотного блока
                    ));

    public static final Supplier<Block> ENHANCED_HATCH_BLOCK =
        BLOCKS.register("enhanced_hatch_block",
            () -> new EnhancedHatchBlock(
                BlockBehaviour.Properties.of()
                    .strength(3.0F, 8.0F) // Прочность и сопротивление взрывам (как у камня/кирпича)
                        .sound(SoundType.METAL) // Звуки шагов и разрушения
                        .requiresCorrectToolForDrops() // Требует кирки для выпадения
                        .instrument(NoteBlockInstrument.BASEDRUM) // Звук нотного блока
                    ));

    public static void register(IEventBus event) {
        BLOCKS.register(event);
    }
}
