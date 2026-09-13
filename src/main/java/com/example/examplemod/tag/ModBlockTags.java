package com.example.examplemod.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import com.example.examplemod.ExampleMod;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockTags {



    public static final TagKey<Block> AFFECTED_BY_CHLORINE_SOIL = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "affected_by_chlorine_soil") // Вместо ExampleMod.prefix(...)
    );
}