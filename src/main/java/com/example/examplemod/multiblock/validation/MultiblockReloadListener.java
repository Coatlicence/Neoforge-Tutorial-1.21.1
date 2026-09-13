package com.example.examplemod.multiblock.validation;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.multiblock.validation.data.MultiblockDefinition;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class MultiblockReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, MultiblockDefinition>> {
    @Override
    protected Map<ResourceLocation, MultiblockDefinition> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        var map = resourceManager.listResources("multiblock", id -> id.getPath().endsWith(".json"));

        if (map.isEmpty())
            ExampleMod.LOGGER.info("list of multiblocks is empty in data directory");

        Map<ResourceLocation, MultiblockDefinition> result = new HashMap<>();

        for (Map.Entry<ResourceLocation, Resource> entry : map.entrySet()) {
            ResourceLocation id = entry.getKey();
            Resource resource = entry.getValue();

            try (Reader reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                DataResult<MultiblockDefinition> parseResult = MultiblockDefinition.CODEC.parse(JsonOps.INSTANCE, json);

                parseResult.ifSuccess(def -> {
                    result.put(def.id(), def);
                    ExampleMod.LOGGER.info("Loaded multiblock structure: {}", def.id());
                });

                parseResult.ifError(error -> {
                    ExampleMod.LOGGER.error("Failed to parse multiblock {}: {}", id, error.message());
                });
            }
            catch (IOException ex) {
                ExampleMod.LOGGER.error("Failed to parse multiblock {}: {}", id, ex);
            }
        }

        return result;
    }

    @Override
    protected void apply(Map<ResourceLocation, MultiblockDefinition> data, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        MultiblockStructures.replace(data);
        ExampleMod.LOGGER.info("Multiblock structures loaded: {} total", data.size());

        Map<Block, AABB> aabbMap = MultiblockStructures.buildBlockAABBMap();
        MultiblockNotifier.registerAABBs(aabbMap);
        ExampleMod.LOGGER.info("Registered AABB for blocks: {} total", aabbMap.size());
    }
}
