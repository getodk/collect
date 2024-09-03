/*
 * This file includes code from MapScaleView (https://github.com/pengrad/MapScaleView),
 * licensed under the Apache License, Version 2.0.
 */
package org.odk.collect.googlemaps.scaleview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;

public class MapScaleView extends View {

    private final MapScaleModel mapScaleModel;
    private final Drawer drawer;

    private final int maxWidth;

    private ScaleType scaleType = ScaleType.BOTH;

    private enum ScaleType {
        METERS_ONLY, MILES_ONLY, BOTH
    }

    public MapScaleView(Context context) {
        this(context, null);
    }

    public MapScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float density = getResources().getDisplayMetrics().density;
        mapScaleModel = new MapScaleModel(density);

        ViewConfig viewConfig = new ViewConfig(context, attrs);
        drawer = new Drawer(viewConfig.color, viewConfig.textSize, viewConfig.strokeWidth, density, viewConfig.outline, viewConfig.expandRtl);

        maxWidth = viewConfig.maxWidth;

        if (viewConfig.isMiles) {
            scaleType = ScaleType.MILES_ONLY;
        }
    }

    public void setTileSize(int tileSize) {
        mapScaleModel.setTileSize(tileSize);
        updateScales();
    }

    public void setColor(@ColorInt int color) {
        drawer.setColor(color);
        invalidate();
    }

    public void setTextSize(float textSize) {
        drawer.setTextSize(textSize);
        invalidate();
        requestLayout();
    }

    public void setTextFont(Typeface font) {
        drawer.setTextFont(font);
        invalidate();
        requestLayout();
    }

    public void setStrokeWidth(float strokeWidth) {
        drawer.setStrokeWidth(strokeWidth);
        invalidate();
        requestLayout();
    }

    public void setOutlineEnabled(boolean enabled) {
        drawer.setOutlineEnabled(enabled);
        invalidate();
    }

    public void setExpandRtlEnabled(boolean enabled) {
        drawer.setExpandRtlEnabled(enabled);
        invalidate();
    }

    /**
     * @deprecated Use milesOnly()
     */
    @Deprecated
    public void setIsMiles(boolean miles) {
        if (miles) {
            milesOnly();
        } else {
            metersAndMiles();
        }
    }

    public void metersOnly() {
        scaleType = ScaleType.METERS_ONLY;
        updateScales();
    }

    public void milesOnly() {
        scaleType = ScaleType.MILES_ONLY;
        updateScales();
    }

    public void metersAndMiles() {
        scaleType = ScaleType.BOTH;
        updateScales();
    }

    public void update(float zoom, double latitude) {
        mapScaleModel.setPosition(zoom, latitude);
        updateScales();
    }

    private void updateScales() {
        Scale top;
        Scale bottom = null;

        if (scaleType == ScaleType.MILES_ONLY) {
            top = mapScaleModel.update(false);
        } else {
            top = mapScaleModel.update(true);
            if (scaleType == ScaleType.BOTH) {
                bottom = mapScaleModel.update(false);
            }
        }

        drawer.setScales(new Scales(top, bottom));
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureDimension(desiredWidth(), widthMeasureSpec);
        int height = measureDimension(desiredHeight(), heightMeasureSpec);

        if (mapScaleModel.updateMaxWidth(width)) {
            updateScales();
        }

        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            width = drawer.getWidth();
        }

        drawer.setViewWidth(width);
        setMeasuredDimension(width, height);
    }

    private int desiredWidth() {
        return maxWidth;
    }

    private int desiredHeight() {
        return drawer.getHeight();
    }

    private int measureDimension(int desiredSize, int measureSpec) {
        int mode = View.MeasureSpec.getMode(measureSpec);
        int size = View.MeasureSpec.getSize(measureSpec);

        if (mode == View.MeasureSpec.EXACTLY) {
            return size;
        } else if (mode == View.MeasureSpec.AT_MOST) {
            return Math.min(desiredSize, size);
        } else {
            return desiredSize;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawer.draw(canvas);
    }
}
