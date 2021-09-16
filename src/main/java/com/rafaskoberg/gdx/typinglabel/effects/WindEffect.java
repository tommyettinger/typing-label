
package com.rafaskoberg.gdx.typinglabel.effects;

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
    private static final float IDEAL_DELTA       = 1 / 60f;

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
        float changeAmount = 0.2f * intensity * DEFAULT_INTENSITY * delta * IDEAL_DELTA;
        noiseCursorX += changeAmount;
        noiseCursorY += changeAmount;
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta) {
        // Calculate progress
        float progressModifier = (1f / intensity) * DEFAULT_INTENSITY;
        float normalSpacing = (1f / spacing) * DEFAULT_SPACING;
        float progressOffset = localIndex / normalSpacing;
        float progress = calculateProgress(progressModifier, progressOffset);

        // Calculate noise
        float indexOffset = localIndex * 0.025f * spacing;
        float noiseX = noise1D(-1234, noiseCursorX + indexOffset);
        float noiseY = noise1D(54321, noiseCursorY + indexOffset);
//        float noiseX = noise.getNoise(noiseCursorX + indexOffset, 0);
//        float noiseY = noise.getNoise(0, noiseCursorY + indexOffset);

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
     * A type of seeded 1D noise that takes an int seed and a float distance, and is optimized for
     * usage on GWT. This uses quintic interpolation between random peak or valley points, with two
     * octaves generated. It is similar to Simplex noise if you only change x or only change y.
     * @param seed an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float noise1D(int seed, float value)
    {
        int floor = value >= 0f ? (int) value : (int) value - 1;
        int z = seed + floor;
        float start = (((z = (z ^ 0xD1B54A35) * 0x102473) ^ (z << 11 | z >>> 21) ^ (z << 19 | z >>> 13)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z) * 0x0.ffffffp-31f;
        float end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x102473) ^ (z << 11 | z >>> 21) ^ (z << 19 | z >>> 13)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z) * 0x0.ffffffp-31f;
        float u = value - floor;
        u *= u * u * (u * (u * 6 - 15) + 10);
        u = (1 - u) * start + u * end;

        seed ^= 0xC13FA9A9;
        value = (value + 1.5f) * 1.6180339887498949f;

        floor = value >= 0f ? (int) value : (int) value - 1;
        z = seed + floor;
        start = (((z = (z ^ 0xD1B54A35) * 0x102473) ^ (z << 11 | z >>> 21) ^ (z << 19 | z >>> 13)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z) * 0x0.ffffffp-31f;
        end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x102473) ^ (z << 11 | z >>> 21) ^ (z << 19 | z >>> 13)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z) * 0x0.ffffffp-31f;
        float v = value - floor;
        v *= v * v * (v * (v * 6 - 15) + 10);
        v = (1 - v) * start + v * end;

        return u * 0.675f + v * 0.325f;
    }

}
