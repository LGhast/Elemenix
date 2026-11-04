package net.lghast.elemenix.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.lghast.elemenix.config.ClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class OutlineRendererHelper {
    public static final OutlineRendererHelper INSTANCE = new OutlineRendererHelper();

    private static final float LINE_WIDTH = 1f / 32f;

    private static final RenderType OUTLINE = RenderType.entityTranslucent(
            ResourceLocation.withDefaultNamespace("textures/misc/white.png"));

    private final Map<Object, BoxEntry> boxes = new HashMap<>();
    private final Map<Object, TextEntry> texts = new HashMap<>();

    public void showShape(Object slot, VoxelShape shape, int color) {
        List<AABB> aabbs = new ArrayList<>();
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                aabbs.add(new AABB(minX, minY, minZ, maxX, maxY, maxZ)));
        boxes.computeIfAbsent(slot, k -> new BoxEntry()).set(aabbs, color);
    }

    public void showBox(Object slot, AABB aabb, int color) {
        boxes.computeIfAbsent(slot, k -> new BoxEntry()).set(List.of(aabb), color);
    }

    public void showText(Object slot, BlockPos pos, float height, List<Component> lines) {
        texts.computeIfAbsent(slot, k -> new TextEntry()).set(pos, height, lines);
    }

    public void clear() {
        boxes.clear();
        texts.clear();
    }

    public void tick() {
        tickMap(boxes);
        tickMap(texts);
    }

    private static <T extends Entry> void tickMap(Map<Object, T> map) {
        Iterator<T> it = map.values().iterator();
        while (it.hasNext()) {
            T entry = it.next();
            entry.ticksTillRemoval--;
            if (entry.ticksTillRemoval < -ClientConfig.ANALYTICAL_WRENCH_FADE_TICKS.get()) {
                it.remove();
            }
        }
    }

    public void render(PoseStack pose, MultiBufferSource buffer, Camera camera) {
        if (boxes.isEmpty() && texts.isEmpty()) return;

        for (BoxEntry entry : boxes.values()) {
            renderBox(pose, buffer.getBuffer(OUTLINE), entry, camera);
        }
        for (TextEntry entry : texts.values()) {
            renderText(pose, buffer, entry, camera);
        }
    }

    private void renderBox(PoseStack pose, VertexConsumer consumer, BoxEntry entry, Camera camera) {
        float alpha = computeAlpha(entry.ticksTillRemoval);
        if (alpha <= 1f / 8f) return;

        Vec3 cam = camera.getPosition();
        float r = srgbToLinear(((entry.color >> 16) & 0xFF) / 255f);
        float g = srgbToLinear(((entry.color >> 8) & 0xFF) / 255f);
        float b = srgbToLinear((entry.color & 0xFF) / 255f);

        Matrix4f mat = pose.last().pose();
        float w = LINE_WIDTH / 2f;
        float inflate = 1f / 128f;

        for (AABB box : entry.aabbs) {
            float x0 = (float) (box.minX - cam.x) - inflate;
            float y0 = (float) (box.minY - cam.y) - inflate;
            float z0 = (float) (box.minZ - cam.z) - inflate;
            float x1 = (float) (box.maxX - cam.x) + inflate;
            float y1 = (float) (box.maxY - cam.y) + inflate;
            float z1 = (float) (box.maxZ - cam.z) + inflate;

            drawCuboid(consumer, mat, x0 - w, y0 - w, z0 - w, x1 + w, y0 + w, z0 + w, r, g, b, alpha);
            drawCuboid(consumer, mat, x0 - w, y1 - w, z0 - w, x1 + w, y1 + w, z0 + w, r, g, b, alpha);
            drawCuboid(consumer, mat, x0 - w, y0 - w, z1 - w, x1 + w, y0 + w, z1 + w, r, g, b, alpha);
            drawCuboid(consumer, mat, x0 - w, y1 - w, z1 - w, x1 + w, y1 + w, z1 + w, r, g, b, alpha);

            drawCuboid(consumer, mat, x0 - w, y0 - w, z0 - w, x0 + w, y1 + w, z0 + w, r, g, b, alpha);
            drawCuboid(consumer, mat, x1 - w, y0 - w, z0 - w, x1 + w, y1 + w, z0 + w, r, g, b, alpha);
            drawCuboid(consumer, mat, x0 - w, y0 - w, z1 - w, x0 + w, y1 + w, z1 + w, r, g, b, alpha);
            drawCuboid(consumer, mat, x1 - w, y0 - w, z1 - w, x1 + w, y1 + w, z1 + w, r, g, b, alpha);

            drawCuboid(consumer, mat, x0 - w, y0 - w, z0 - w, x0 + w, y0 + w, z1 + w, r, g, b, alpha);
            drawCuboid(consumer, mat, x1 - w, y0 - w, z0 - w, x1 + w, y0 + w, z1 + w, r, g, b, alpha);
            drawCuboid(consumer, mat, x0 - w, y1 - w, z0 - w, x0 + w, y1 + w, z1 + w, r, g, b, alpha);
            drawCuboid(consumer, mat, x1 - w, y1 - w, z0 - w, x1 + w, y1 + w, z1 + w, r, g, b, alpha);
        }
    }

    private void drawCuboid(VertexConsumer consumer, Matrix4f mat, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b, float a) {
        int light = LightTexture.FULL_BRIGHT;
        int overlay = OverlayTexture.NO_OVERLAY;
        // down
        vertex(consumer, mat, x0, y0, z0, 0, -1, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x1, y0, z0, 0, -1, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x1, y0, z1, 0, -1, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x0, y0, z1, 0, -1, 0, r, g, b, a, light, overlay);
        // up
        vertex(consumer, mat, x0, y1, z1, 0, 1, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x1, y1, z1, 0, 1, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x1, y1, z0, 0, 1, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x0, y1, z0, 0, 1, 0, r, g, b, a, light, overlay);
        // north
        vertex(consumer, mat, x0, y1, z0, 0, 0, -1, r, g, b, a, light, overlay);
        vertex(consumer, mat, x1, y1, z0, 0, 0, -1, r, g, b, a, light, overlay);
        vertex(consumer, mat, x1, y0, z0, 0, 0, -1, r, g, b, a, light, overlay);
        vertex(consumer, mat, x0, y0, z0, 0, 0, -1, r, g, b, a, light, overlay);
        // south
        vertex(consumer, mat, x0, y0, z1, 0, 0, 1, r, g, b, a, light, overlay);
        vertex(consumer, mat, x1, y0, z1, 0, 0, 1, r, g, b, a, light, overlay);
        vertex(consumer, mat, x1, y1, z1, 0, 0, 1, r, g, b, a, light, overlay);
        vertex(consumer, mat, x0, y1, z1, 0, 0, 1, r, g, b, a, light, overlay);
        // west
        vertex(consumer, mat, x0, y0, z0, -1, 0, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x0, y0, z1, -1, 0, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x0, y1, z1, -1, 0, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x0, y1, z0, -1, 0, 0, r, g, b, a, light, overlay);
        // east
        vertex(consumer, mat, x1, y1, z0, 1, 0, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x1, y1, z1, 1, 0, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x1, y0, z1, 1, 0, 0, r, g, b, a, light, overlay);
        vertex(consumer, mat, x1, y0, z0, 1, 0, 0, r, g, b, a, light, overlay);
    }

    private void vertex(VertexConsumer consumer, Matrix4f mat, float x, float y, float z, float nx, float ny, float nz, float r, float g, float b, float a, int light, int overlay) {
        consumer.addVertex(mat, x, y, z).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
    }

    private void renderText(PoseStack pose, MultiBufferSource buffer, TextEntry entry, Camera camera) {
        float alpha = computeAlpha(entry.ticksTillRemoval);
        if (alpha <= 1f / 8f) return;

        int color = ((int) (alpha * 255) << 24) | 0x00FFFFFF;

        Vec3 cam = camera.getPosition();
        double x = entry.pos.getX() + 0.5 - cam.x;
        float lineHeight = 10f;
        float scale = ClientConfig.ANALYTICAL_WRENCH_TEXT_SCALE.get().floatValue();
        double y = entry.pos.getY() + entry.height + (entry.lines.size() - 1) * lineHeight * scale - cam.y;
        double z = entry.pos.getZ() + 0.5 - cam.z;

        Font font = Minecraft.getInstance().font;

        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(camera.rotation());
        pose.scale(scale, -scale, scale);
        for (int i = 0; i < entry.lines.size(); i++) {
            Component line = entry.lines.get(i);
            float halfWidth = -font.width(line) / 2f;
            float lineY = i * lineHeight;
            font.drawInBatch(line, halfWidth, lineY, color, false,
                    pose.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
        }
        pose.popPose();
    }

    private static float computeAlpha(int ticksTillRemoval) {
        if (ticksTillRemoval >= 0) return 1f;
        float t = 1f + (float) ticksTillRemoval / ClientConfig.ANALYTICAL_WRENCH_FADE_TICKS.get();
        return Math.max(0f, t * t * t);
    }

    private static float srgbToLinear(float c) {
        return c <= 0.04045f ? c / 12.92f : (float) Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static class Entry {
        int ticksTillRemoval = 1;
    }

    private static class BoxEntry extends Entry {
        List<AABB> aabbs = List.of();
        int color = 0xFFFFFF;

        void set(List<AABB> aabbs, int color) {
            this.aabbs = aabbs;
            this.color = color;
            this.ticksTillRemoval = 1;
        }
    }

    private static class TextEntry extends Entry {
        BlockPos pos = BlockPos.ZERO;
        float height = 1.0f;
        List<Component> lines = List.of();

        void set(BlockPos pos, float height, List<Component> lines) {
            this.pos = pos;
            this.height = height;
            this.lines = lines;
            this.ticksTillRemoval = 1;
        }
    }
}
