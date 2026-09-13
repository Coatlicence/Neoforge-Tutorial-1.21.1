package com.example.examplemod.client.render;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.blockentities.multiblock.controller.EnhancedBlastFurnaceOutputBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

import static com.example.examplemod.block.multiblock.controller.EnhancedBlastFurnaceOutput.FORMED;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class MultiblockHighlightRenderer {

    @SubscribeEvent
    public static void onDrawBlockHighlight(RenderHighlightEvent.Block event) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        BlockHitResult target = event.getTarget();
        BlockPos pos = target.getBlockPos();
        BlockEntity be = level.getBlockEntity(pos);

        // 1. Диагностический лог: проверяем, вообще ли срабатывает перехват для этого блока
        if (be instanceof EnhancedBlastFurnaceOutputBlockEntity controller) {
            ExampleMod.LOGGER.debug("КЛИЕНТ: Наведение на контроллер многоблока на {}", pos);

            boolean isFormed = controller.getBlockState().getValue(FORMED);
            BlockPos failedPos = controller.getMultiblockState().getFailedPos();

            if (isFormed) {
                // Структура цела. Не рисуем красную рамку, позволяем отрисоваться стандартной белой.
                return;
            }

            if (failedPos == null) {
                ExampleMod.LOGGER.warn("КЛИЕНТ: Структура не собрана, но failedPos == null! Проверьте MultiblockValidator.");
                return; // Позволяем отрисоваться стандартной белой рамке для отладки
            }

            ExampleMod.LOGGER.info("КЛИЕНТ: Рендерим подсветку ошибки на {}", failedPos);

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource bufferSource = event.getMultiBufferSource();
            Camera camera = event.getCamera();
            Vec3 cameraPos = camera.getPosition();

            poseStack.pushPose();

            // Сдвиг матрицы относительно камеры (стандартный паттерн Minecraft)
            poseStack.translate(
                    failedPos.getX() - cameraPos.x,
                    failedPos.getY() - cameraPos.y,
                    failedPos.getZ() - cameraPos.z
            );

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

            // Vanilla-отступ (0.002f) предотвращает Z-fighting с текстурой блока
            float expand = 0.002F;
            LevelRenderer.renderLineBox(
                    poseStack,
                    vertexConsumer,
                    0.0F - expand, 0.0F - expand, 0.0F - expand,
                    1.0F + expand, 1.0F + expand, 1.0F + expand,
                    1.0F, 0.0F, 0.0F, 1.0F // Ярко-красный, полная непрозрачность
            );

            poseStack.popPose();

            // Отменяем стандартную белую рамку ТОЛЬКО если мы успешно нарисовали красную
            event.setCanceled(true);
        }
    }
}