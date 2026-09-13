package com.example.examplemod;

import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.blockentities.multiblock.component.EnhancedHatchBlockEntity;
import com.example.examplemod.blockentities.multiblock.controller.EnhancedBlastFurnaceOutputBlockEntity;
import com.example.examplemod.blockentities.ModBlockEntities;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.multiblock.MultiblockReloadListener;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {

    public static final String MODID = "examplemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(this::onRegisterCapabilities);
        
        NeoForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        EnhancedHatchBlockEntity.registerCapabilities(event);
    }


    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));

        //EnhancedBlastFurnaceOutputBlockEntity.registerProfiles();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        LOGGER.info(">>> onAddReloadListeners вызван!");
        event.addListener(new MultiblockReloadListener());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            event.accept(ModItems.BISMUTH);
        }
    }
}
