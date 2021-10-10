
package com.rafaskoberg.gdx.typinglabel.effects;

import com.badlogic.gdx.utils.NumberUtils;
import com.rafaskoberg.gdx.typinglabel.Effect;
import com.rafaskoberg.gdx.typinglabel.TypingGlyph;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;

/** Moves the text in a wind pattern. */
public class WindEffect extends Effect {
    private static final float DEFAULT_SPACING   = 10f;
    private static final float DEFAULT_DISTANCE  = 0.33f;
    private static final float DEFAULT_INTENSITY = 0.375f;
    private static final float DISTANCE_X_RATIO  = 1.5f;
    private static final float DISTANCE_Y_RATIO  = 1.0f;
    private static final float IDEAL_DELTA       = 60f;

    private float        noiseCursorX = 0;
    private float        noiseCursorY = 0;

    private float distanceX = 1; // How much of their line height glyphs should move in the X axis
    private float distanceY = 1; // How much of their line height glyphs should move in the Y axis
    private float spacing   = 1; // How much space there should be between waves
    private float intensity = 1; // How strong the wind should be

    public WindEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance X
        if(params.length > 0) {
            this.distanceX = paramAsFloat(params[0], 1);
        }

        // Distance Y
        if(params.length > 1) {
            this.distanceY = paramAsFloat(params[1], 1);
        }

        // Spacing
        if(params.length > 2) {
            this.spacing = paramAsFloat(params[2], 1);
        }

        // Intensity
        if(params.length > 3) {
            this.intensity = paramAsFloat(params[3], 1);
        }

        // Duration
        if(params.length > 4) {
            this.duration = paramAsFloat(params[4], -1);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update noise cursor
        float changeAmount = 0.15f * intensity * DEFAULT_INTENSITY * delta * IDEAL_DELTA;
        noiseCursorX += changeAmount;
        noiseCursorY += changeAmount;
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta) {
        // Calculate progress
        float progressModifier = DEFAULT_INTENSITY / intensity;
        float normalSpacing = DEFAULT_SPACING / spacing;
        float progressOffset = localIndex / normalSpacing;
        float progress = calculateProgress(progressModifier, progressOffset);

        // Calculate noise
        float indexOffset = localIndex * 0.05f * spacing;
        float noiseX = octaveNoise1D(noiseCursorX + indexOffset, 123);
        float noiseY = octaveNoise1D(noiseCursorY + indexOffset, -4321);

        // Calculate offset
        float lineHeight = getLineHeight();
        float x = lineHeight * noiseX * progress * distanceX * DISTANCE_X_RATIO * DEFAULT_DISTANCE;
        float y = lineHeight * noiseY * progress * distanceY * DISTANCE_Y_RATIO * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        x *= fadeout;
        y *= fadeout;

        // Add flag effect to X offset
        x = Math.abs(x) * -Math.signum(distanceX);

        // Apply changes
        glyph.xoffset += x;
        glyph.yoffset += y;
    }

    /**
     * Quilez' 1D noise, with some changes to work on the CPU. Takes a distance x and any int seed, and produces a
     * smoothly-changing value as x goes up or down and seed stays the same. Uses a quartic curve.
     * @param x should go up and/or down steadily and by small amounts (less than 1.0, certainly)
     * @param seed should stay the same for a given curve
     * @return a noise value between -1.0 and 1.0
     */
    private static float noise1D(float x, final int seed) {
        x += seed * 0x1p-24f;
        final int xFloor = x >= 0f ? (int) x : (int) x - 1,
                rise = 1 - ((x >= 0f ? (int) (x + x) : (int) (x + x) - 1) & 2);
        x -= xFloor;
        final float h = NumberUtils.intBitsToFloat((int)((seed + xFloor ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L >>> 41) | 0x42000000) - 48f;
        x *= x - 1f;
        return rise * x * x * h;
    }

    /**
     * Just gets two octaves of {@link #noise1D(float, int)}; still has a range of -1 to 1.
     * @param x should go up and/or down steadily and by small amounts (less than 1.0, certainly)
     * @param seed should stay the same for a given curve
     * @return a noise value between -1.0 and 1.0
     */
    private static float octaveNoise1D(float x, int seed){
        return (noise1D(x, seed) * 2f + noise1D(x * 1.9f, ~seed)) * 0.3333333333333333f;
    }

}
