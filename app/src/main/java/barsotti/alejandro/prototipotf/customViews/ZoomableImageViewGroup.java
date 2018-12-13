package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.Utils.MathUtils;

import static barsotti.alejandro.prototipotf.customViews.Shape.TOUCH_RADIUS;

public class ZoomableImageViewGroup extends FrameLayout {
    private static final String TAG = "ZoomableImageViewGroup";
    private ZoomableImageView mZoomableImageView;
    private ArrayList<Shape> mShapeList = new ArrayList<>();
    private Shape mInProgressShape;
    private Shape mCurrentlySelectedShape;

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

    public void setZoomableImageViewDrawingInProgress(boolean drawingInProgress, Class shapeClass) {
        if (!drawingInProgress) {
            mInProgressShape = null;
        }
        else {
            try {
                // TODO: Verificar que no se esté dibujando ya una figura. En tal caso, debería eliminarse
                // y eliminar todos los listeners que se hayan seteado.

                checkCanDrawShape(shapeClass);

                Class<?> myClassType = Class.forName(shapeClass.getName());

//                for (Shape shape: mShapeList) {
//                    if (shape.getClass() == Circle.class) {
//                        String message = "No es posible dibujar más de una circunferencia";
//                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
//                        throw new InstantiationException(message);
//                    }
//                }

                Class<?>[] types = new Class[] { Context.class };
                Constructor<?> cons = myClassType.getConstructor(types);
                mInProgressShape = (Shape) cons.newInstance(getContext());
                mInProgressShape.addShapeCreatorListener(mZoomableImageView);
                mInProgressShape.selectShape(true);
                for (Shape shape: mShapeList) {
                    shape.selectShape(false);
                }
                mShapeList.add(mInProgressShape);
                this.addView(mInProgressShape);
            } catch (Exception e) {
                Log.d(TAG, "setZoomableImageViewDrawingInProgress:");
                e.printStackTrace();
                drawingInProgress = false;
            }
        }
        mZoomableImageView.setDrawingInProgress(drawingInProgress);
    }

    private void checkCanDrawShape(Class shapeClass) throws Exception {
        // Caso: Tangente. No es posible dibujar una tangente si no existe una circunferencia previamente.
        if (shapeClass == Tangent.class) {
            boolean canDrawTangent = false;
            for (Shape shape: mShapeList) {
                if (shape.getClass() == Circle.class) {
                    canDrawTangent = true;
                    break;
                }
            }

            if (!canDrawTangent) {
                Toast.makeText(getContext(), R.string.cannot_draw_tangent_message,
                    Toast.LENGTH_SHORT).show();
                throw new Exception();
            }
        }
    }

    public void setupZoomableImageView(int displayWidth, int displayHeight, Uri uri, Drawable drawable) {
        mZoomableImageView.setupZoomableImageView(displayWidth, displayHeight, uri, drawable);
    }

    public void addPointToInProgressShape(PointF point) {
//        if (mInProgressShape == null) {
//            mInProgressShape = new Circle(getContext(), mZoomableImageView);
//            mInProgressShape.selectShape(true);
//            for (Shape shape: mShapeList) {
//                shape.selectShape(false);
//            }
//            mShapeList.add(mInProgressShape);
//            this.addView(mInProgressShape);
//        }

        if (mInProgressShape != null) {
            // Caso: Tangente. Determinar a qué circunferencia corresponde asociar la tangente.
            if (mInProgressShape.getClass() == Tangent.class) {
                Circle closestCircle = null;
                double distance = -1;
                for (Shape shape : mShapeList) {
                    if (shape.getClass() == Circle.class) {
                        PointF circleCenter = ((Circle) shape).mMappedCenter;
                        double newDistance = Math.abs(MathUtils.distanceBetweenPoints(point.x, point.y,
                            circleCenter.x, circleCenter.y) - ((Circle) shape).mMappedRadius);
                        if (distance < 0 || newDistance < distance) {
                            distance = newDistance;
                            closestCircle = (Circle) shape;
                        }
                    }
                }

                if (closestCircle != null) {
                    closestCircle.addOnCircleCenterChangeListener((Tangent) mInProgressShape);
                }
            }

            // Agregar punto a la forma y determinar si el dibujo fue finalizado.
            boolean finishedShape = !mInProgressShape.addPoint(point);

            if (finishedShape) {
                setZoomableImageViewDrawingInProgress(false, null);
            }
        }

//        mShapeList.add(circle);
//        this.addView(circle);
//        mZoomableImageView.addOnMatrixViewChangeListener(circle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    public void checkShapeSelection(PointF point) {
        float minDistance = TOUCH_RADIUS + 1;

        for (int i = mShapeList.size() - 1; i >= 0; i--) {
            float newDistance = mShapeList.get(i).computeDistanceBetweenTouchAndShape(point);
            if (newDistance > 0 && newDistance < minDistance) {
                minDistance = newDistance;
                mCurrentlySelectedShape = mShapeList.get(i);
            }
        }

        if (minDistance > TOUCH_RADIUS) {
            mCurrentlySelectedShape = null;
        }

        // Deseleccionar todas las formas excepto la seleccionada (en caso de que haya).
        for (Shape shape: mShapeList) {
            shape.selectShape(shape == mCurrentlySelectedShape);
        }
    }
}
