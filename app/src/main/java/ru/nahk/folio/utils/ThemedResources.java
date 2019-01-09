package ru.nahk.folio.utils;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

/**
 * Helper class to access themed attributes.
 */
public class ThemedResources {
    /**
     * Gets themed color attribute.
     * @param context Current context.
     * @param themeAttributeId Attribute resource identifier.
     * @param fallbackColor Color to use, if attribute value is not known.
     * @return Color value.
     */
    @ColorInt
    public static int getColor(Context context, int themeAttributeId, int fallbackColor) {
        TypedValue outValue = new TypedValue();

        if (context.getTheme().resolveAttribute(themeAttributeId, outValue, true)) {
            return outValue.resourceId == 0
                ? outValue.data
                : ContextCompat.getColor(context, outValue.resourceId);
        } else {
            return fallbackColor;
        }
    }
}
