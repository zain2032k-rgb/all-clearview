package com.clearview;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/** Replacement transforms for the first-person item swing. p = swing progress 0..1, side = +1 right hand / -1 left hand. */
public final class SwingMath {
    private static final float PI = (float) Math.PI;

    public static void apply(SwingStyle style, float p, MatrixStack m, int side, float k) {
        float sq = MathHelper.sin(MathHelper.sqrt(p) * PI);
        float lin = MathHelper.sin(p * PI);
        float ease = p * p * (3f - 2f * p);

        switch (style) {
            case SMOOTH -> {
                m.translate(0f, 0.04f * sq * k, -0.20f * sq * k);
                rx(m, -55f * sq * k);
                ry(m, side * -12f * lin * k);
                rz(m, side * -8f * sq * k);
            }
            case SLIDE -> {
                m.translate(-side * 0.50f * sq * k, 0.05f * lin * k, -0.15f * lin * k);
                ry(m, side * 40f * sq * k);
                rz(m, side * 25f * sq * k);
            }
            case STAB -> {
                m.translate(0f, 0.04f * lin * k, -0.85f * lin * k);
                rx(m, -10f * lin * k);
            }
            case SPIN -> {
                m.translate(0f, 0.05f * lin * k, -0.25f * lin * k);
                rz(m, side * 360f * ease * k);
            }
            case SWIRL -> {
                float a = p * 2f * PI;
                m.translate(0.28f * MathHelper.sin(a) * k, 0.28f * (1f - MathHelper.cos(a)) * 0.5f * k, -0.20f * lin * k);
                rz(m, side * -50f * MathHelper.sin(a) * k);
                rx(m, -25f * lin * k);
            }
            case CHOP -> {
                m.translate(0f, 0.25f * lin * k, -0.35f * lin * k);
                rx(m, -100f * lin * k);
                rz(m, side * -10f * lin * k);
            }
            default -> { }
        }
    }

    private static void rx(MatrixStack m, float deg) { m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(deg)); }
    private static void ry(MatrixStack m, float deg) { m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(deg)); }
    private static void rz(MatrixStack m, float deg) { m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(deg)); }

    private SwingMath() {}
}
