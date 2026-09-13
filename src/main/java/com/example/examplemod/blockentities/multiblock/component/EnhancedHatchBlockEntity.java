package com.example.examplemod.blockentities.multiblock.component;

import com.example.examplemod.blockentities.ModBlockEntities;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EnhancedHatchBlockEntity extends SmartBlockEntity {
    public EnhancedHatchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENHANCED_HATCH.get(), pos, state);

        // Создаём инвентарь на 1 слот, макс 64 предмета
        this.inventory = new SmartInventory(1, this).withMaxStackSize(64);

        // Подписываемся на изменения инвентаря
        this.inventory.whenContentsChanged(this::onInventoryChanged);
    }

    public SmartInventory inventory;

    ///-------------------------------------------------------
    /// EVENTS: All can subscribe for him
    private final List<Consumer<Integer>> changeCallbacks = new ArrayList<>();

    // Публичный API для контроллеров
    public void addChangeCallback(Consumer<Integer> callback) {
        changeCallbacks.add(callback);
    }

    public void removeChangeCallback(Consumer<Integer> callback) {
        changeCallbacks.remove(callback);
    }

    public void clearChangeCallbacks() {
        changeCallbacks.clear();
    }

    // Вызывается при любом изменении инвентаря
    private void onInventoryChanged(int slot) {
        // Уведомляем ВСЕ подписанные контроллеры
        for (Consumer<Integer> callback : changeCallbacks) {
            callback.accept(slot);
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ENHANCED_HATCH.get(),
                (be, context) -> be.inventory
        );
    }

    ///----------------------------------------------
    // DATA: Save & Load
    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Inventory", this.inventory.serializeNBT(registries));
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        this.inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
    }


    ///------------------------------------------------
    // LOGIC
    @Override
    public void destroy() {
        ItemHelper.dropContents(this.level, this.worldPosition, this.inventory);
        super.destroy();

    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> list) {

    }
}
