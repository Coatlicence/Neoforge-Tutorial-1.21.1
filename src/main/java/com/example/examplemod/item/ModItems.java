package com.example.examplemod.item;

import com.example.examplemod.ExampleMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.example.examplemod.block.ModBlocks.*;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExampleMod.MODID);

    public static final DeferredItem<Item> BISMUTH = ITEMS.register("bismuth",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> AMMONIUM_CHLORIDE_RICH_SOIL_ITEM =
            ITEMS.register("ammonium_chloride_rich_soil",
                    () -> new BlockItem(AMMONIUM_CHLORIDE_RICH_SOIL.get(), new Item.Properties())
            );

    public static final Supplier<Item> AMMONIUM_CHLORIDE_RICH_SOIL_FARMLAND_ITEM =
            ITEMS.register("ammonium_chloride_rich_soil_farmland",
                    () -> new BlockItem(AMMONIUM_CHLORIDE_RICH_SOIL_FARMLAND.get(), new Item.Properties())
            );

    public static final Supplier<Item> ENHANCED_FIREPROOF_BRICKS_ITEM =
            ITEMS.register("enhanced_fireproof_bricks",
                    () -> new BlockItem(ENHANCED_FIREPROOF_BRICKS.get(), new Item.Properties())
            );

    public static final Supplier<Item> ENHANCED_BLAST_FURNACE_OUTPUT_ITEM =
            ITEMS.register("enhanced_blast_furnace_output",
                    () -> new BlockItem(ENHANCED_BLAST_FURNACE_OUTPUT.get(), new Item.Properties())
            );

    public static final Supplier<Item> ENHANCED_HATCH_ITEM =
            ITEMS.register("enhanced_hatch_block",
                    () -> new BlockItem(ENHANCED_HATCH_BLOCK.get(), new Item.Properties())
            );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
