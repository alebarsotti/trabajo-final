package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

import barsotti.alejandro.prototipotf.R;

public class ZoomableImageViewGroup extends FrameLayout {
    private static final String TAG = "ZoomableImageViewGroup";
    private ZoomableImageView mZoomableImageView;
    private ArrayList<Shape> mShapeList = new ArrayList<>();
    private Shape mInProgressShape;


    // region Constructors
    public ZoomableImageViewGroup(@NonNull Context context) {
        this(context, null);
    }

    public ZoomableImageViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.zoomable_image_view_group, this, true);
        mZoomableImageView = view.findViewById(R.id.zoomable_image_view);
    }
    // endregion

    public void setZoomableImageViewState(ZoomableImageView.State state) {
        mZoomableImageView.setState(state);
    }

    // TODO: Prueba. Revisar.
    public void setZoomableImageViewDrawingInProgress(boolean drawingInProgress) {
        if (!drawingInProgress) {
            mInProgressShape = null;
        }
        mZoomableImageView.setDrawingInProgress(drawingInProgress);
    }

    public void setupZoomableImageView(int displayWidth, int displayHeight, Uri uri, Drawable drawable) {
        mZoomableImageView.setupZoomableImageView(displayWidth, displayHeight, uri, drawable);
    }

    public void addPointToInProgressShape(PointF point) {
        if (mInProgressShape == null) {
            mInProgressShape = new Circle(getContext(), mZoomableImageView);
            mInProgressShape.selectShape(true);
            for (Shape shape: mShapeList) {
                shape.selectShape(false);
            }
            mShapeList.add(mInProgressShape);
            this.addView(mInProgressShape);
        }

        if (!mInProgressShape.addPoint(point)) {
            setZoomableImageViewDrawingInProgress(false);
        }

//        mShapeList.add(circle);
//        this.addView(circle);
//        mZoomableImageView.addOnMatrixViewChangeListener(circle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        Log.d(TAG, "onInterceptTouchEvent: " + String.valueOf(ev));
        return super.onInterceptTouchEvent(ev);
    }

    public void checkShapeSelection(PointF point) {
        for (int i = mShapeList.size() - 1; i >= 0; i--) {
            if (mShapeList.get(i).verifyShapeTouched(point)) {
                return;
            }
        }


        // FIXME: v1. La lista debería recorrerse en orden inverso, preferentemente.
//        for (Shape shape : mShapeList) {
//            if (shape.verifyShapeTouched(point)) {
//                return;
//            }
//        }
    }

//    // FIXME: Prueba!
//    public void verifyShapeTouched(MotionEvent event) {
//        for (Shape shape: mShapeList) {
//            shape.verifyShapeTouched(event);
//        }
//    }
}
