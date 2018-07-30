package barsotti.alejandro.prototipotf.customViews;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import barsotti.alejandro.prototipotf.Utils.ImageTile;
import barsotti.alejandro.prototipotf.Utils.ImageTileLruCache;
import barsotti.alejandro.prototipotf.interfaces.IZoomableImageView;

public class ZoomableImageView extends android.support.v7.widget.AppCompatImageView
    implements IZoomableImageView {
    private static final int MAX_ZOOM_SCALE = 10;
    private static final int ANIMATION_DURATION = 250;
    private static final int TILE_WIDTH = 64;
    //FIXME
    private static final float TOUCH_TOLERANCE = 10;

    private float mMaxScaleFactor;
    private float mMinScaleFactor;
    private Integer mBitmapWidth;
    private Integer mBitmapHeight;
    private Matrix mDefaultMatrix;
    private Matrix mCurrentMatrix;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;
    private States mState = States.None;

    private PointF mStart;
    private PointF mDrawingStart;
    private PointF mEnd;
    private PointF mDrawingEnd;

    //FIXME: Prueba RegionDecoder
    private Uri mImageUri;
    private Bitmap mImageRegion;
    private Integer mDisplayWidth;
    private Integer mDisplayHeight;
    private Paint mAlphaPaint;
    private AsyncImageRegionDecoder mAsyncImageRegionDecoder;
    private AsyncComputeVisibleTiles mAsyncComputeVisibleTiles;
    private RectF mDrawRectF = new RectF();
    private float mOriginalZoom;
//    private List<ImageTile> mTiles = new ArrayList<>();
    private final Set<ImageTile> mTilesDraw = new HashSet<>();
    private ImageTileLruCache mImageTileCache;
    private Integer mOriginalImageWidth;
    private Integer mOriginalImageHeight;
    private Matrix mOriginalMatrix;
    private BitmapRegionDecoder mBitmapRegionDecoder;
    private BitmapFactory.Options mBitmapOptions;

    // FIXME
    private Path mPath;
    private Path mRealPath;
    private float mX;
    private float mY;

    private Paint mPaint;

    // FIXME: Prueba RegionDecoder
    @Override
    public void setRegionBitmap(Bitmap bitmap) {
        mImageRegion = bitmap;
        invalidate();
    }

    @Override
    public void setRegionRect(RectF rectF) {
        mDrawRectF.set(rectF);
        invalidate();
    }

    public enum States {
        None,
        Scrolling,
        Animating,
        Drawing
    }

    //region Constructors
    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeMembers();
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeMembers();
    }

    public ZoomableImageView(Context context) {
        super(context);
        initializeMembers();
    }
    //endregion

    public void setState(States state) {
        mState = state;
    }









    private void updateImageMatrix() {
        if (mCurrentMatrix.equals(mDefaultMatrix)) {
            mImageRegion = null;
        } else {
            // FIXME: Prueba RegionDecoder
//            ComputeVisibleTiles();

            if (mAsyncComputeVisibleTiles != null) {
                mAsyncComputeVisibleTiles.cancel(true);
            }
            mAsyncComputeVisibleTiles = new AsyncComputeVisibleTiles(this);
            mAsyncComputeVisibleTiles.execute();



//            if (mAsyncImageRegionDecoder != null) {
//                mAsyncImageRegionDecoder.cancel(true);
//            }
//            mImageRegion = null;
//            mAsyncImageRegionDecoder = new AsyncImageRegionDecoder(this);
//            mAsyncImageRegionDecoder.execute();


        }

        this.setImageMatrix(mCurrentMatrix);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.performClick();

        // Verificar si se está en modo de dibujo o no.
        if (!mState.equals(States.Drawing)) {
            // Informar del evento a los detectores de gestos y escala.
            mScaleGestureDetector.onTouchEvent(event);
            mGestureDetector.onTouchEvent(event);

            // Verificar necesidad de desplazamiento al finalizar un scroll.
            if (event.getAction() == MotionEvent.ACTION_UP && mState.equals(States.Scrolling)) {
                setState(States.None);
                handleScrollEnded();
            }

            updateImageMatrix();
        }
        else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    //FIXME: Prueba Path
                    if (mPath == null) {
                        mPath = new Path();
//                        mPath.setFillType(Path.FillType.);
                    }
                    mPath.reset();
                    float x = event.getX();
                    float y = event.getY();
                    mPath.moveTo(x, y);
                    mX = x;
                    mY = y;

//
//                    float[] matrixValues = new float[9];
//                    mCurrentMatrix.getValues(matrixValues);
//                    float offsetX = matrixValues[Matrix.MTRANS_X];
//                    float offsetY = matrixValues[Matrix.MTRANS_Y];
//                    float scale = matrixValues[Matrix.MSCALE_X];
//                    float bitmapX = (event.getX() - offsetX) / scale;
//                    float bitmapY = (event.getY() - offsetY) / scale;
//
//                    if (mStart == null) {
//                        mStart = new PointF(bitmapX, bitmapY);
//                    }
//                    else {
//                        mStart.set(bitmapX, bitmapY);
//                    }
//
//                    mDrawingStart = new PointF(event.getX(), event.getY());
//
//                    mEnd = null;

                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    //FIXME
                    float x = event.getX();
                    float y = event.getY();
                    float dx = Math.abs(x - mX);
                    float dy = Math.abs(y - mY);
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                        mX = x;
                        mY = y;

//                        circlePath.reset();
//                        circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
                    }


//                    float[] matrixValues = new float[9];
//                    mCurrentMatrix.getValues(matrixValues);
//                    float offsetX = matrixValues[Matrix.MTRANS_X];
//                    float offsetY = matrixValues[Matrix.MTRANS_Y];
//                    float scale = matrixValues[Matrix.MSCALE_X];
//                    float bitmapX = (event.getX() - offsetX) / scale;
//                    float bitmapY = (event.getY() - offsetY) / scale;
//
//                    if (mEnd == null) {
//                        mEnd = new PointF(bitmapX, bitmapY);
//                    }
//                    else {
//                        mEnd.set(bitmapX, bitmapY);
//                    }
//                    mDrawingEnd = new PointF(event.getX(), event.getY());
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    //FIXME
                    mPath.lineTo(mX, mY);
//                    circlePath.reset();
//                    // commit the path to our offscreen
//                    mCanvas.drawPath(mPath,  mPaint);
//                    // kill this so we don't double draw
//                    mPath.reset();

                    mRealPath = new Path(mPath);
                    Matrix inverse = new Matrix();
                    mCurrentMatrix.invert(inverse);
                    mRealPath.transform(inverse);

                    setState(States.None);
                    break;
                }
            }
            invalidate();
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //FIXME: Prueba RegionDecoder
//        if (mImageRegion != null && mDrawRectF != null) {
//            RectF rectangle = new RectF(mDrawRectF);
//            mCurrentMatrix.mapRect(rectangle);
//            canvas.drawBitmap(mImageRegion, null, rectangle, mPaint);
//        }
//        if (mTilesDraw == null || mTilesDraw.isEmpty()) {
//            mTilesDraw = new ArrayList<>(mTiles);
//        }








//        mTilesDraw = new ArrayList<>(mTiles);
        synchronized (mTilesDraw) {
            for (ImageTile imageTile: mTilesDraw) {
                mOriginalMatrix.mapRect(mDrawRectF, imageTile.mTileRect);
                mCurrentMatrix.mapRect(mDrawRectF);
                Bitmap bitmap = mImageTileCache.get(imageTile.getKey());
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, null, mDrawRectF, null);
                }
//                canvas.drawRect(mDrawRectF, mPaint);
            }
        }















//        if (mDrawingStart != null && mDrawingEnd != null) {
//            canvas.drawLine(mDrawingStart.x, mDrawingStart.y, mDrawingEnd.x, mDrawingEnd.y, mPaint);
//        }

        //FIXME
//        if (mPath != null) {
//            canvas.drawPath(mPath, mPaint);
//        }
    }

    private void handleScrollEnded() {
        // Verificar que no se vean zonas negras fuera de la imagen. En tal caso, ajustar el desplazamiento.
        final float[] matrixValues = new float[9];
        mCurrentMatrix.getValues(matrixValues);

        // Si el factor de escala es menor al mínimo, no realizar ninguna acción.
        if (matrixValues[Matrix.MSCALE_X] < mMinScaleFactor) {
            return;
        }

        // Desplazamiento actual.
        final float transX = matrixValues[Matrix.MTRANS_X];
        final float transY = matrixValues[Matrix.MTRANS_Y];

        // Dimensiones actuales de la imagen.
        float bitmapWidth = mBitmapWidth * matrixValues[Matrix.MSCALE_X];
        float bitmapHeight = mBitmapHeight * matrixValues[Matrix.MSCALE_Y];

        // Dimensiones de la view.
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Límites de desplazamiento.
        float dX = viewWidth - bitmapWidth;
        float minTransX = dX < 0 ? dX : 0;
        float maxTransX = dX < 0 ? 0 : dX;

        float dY = viewHeight - bitmapHeight;
        float minTransY = dY < 0 ? dY : 0;
        float maxTransY = dY < 0 ? 0 : dY;

        float deltaTransX = 0;
        float deltaTransY = 0;
        // Si el desplazamiento no se encuentra dentro de los límites permitidos. Calcular delta.
        if (transX > maxTransX || transX < minTransX) {
            float limitX = transX > maxTransX ? maxTransX : minTransX;
            deltaTransX = limitX - transX;
        }
        // Si el ancho de la imagen es menor al ancho de la pantalla, centrar horizontalmente.
        if (viewWidth > bitmapWidth) {
            deltaTransX = (viewWidth - bitmapWidth) / 2 - transX;
        }

        // Si el desplazamiento no se encuentra dentro de los límites permitidos. Calcular delta.
        if (transY > maxTransY || transY < minTransY) {
            float limitY = transY > maxTransY ? maxTransY : minTransY;
            deltaTransY = limitY - transY;
        }
        // Si la altura de la imagen es menor a la altura de la pantalla, centrar verticalmente.
        if (viewHeight > bitmapHeight) {
            deltaTransY = (viewHeight - bitmapHeight) / 2 - transY;
        }

        // Si no es necesario desplazar la matriz, no realizar ninguna acción.
        if (deltaTransX == 0 && deltaTransY == 0) {
            return;
        }

        // Desplazar la matriz.
        // mCurrentMatrix.postTranslate(deltaTransX, deltaTransY);
        ValueAnimator transValueAnimator = ValueAnimator.ofFloat(0, 1);
        transValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        transValueAnimator.setDuration(ANIMATION_DURATION);
        final float finalDeltaTransX = deltaTransX;
        final float finalDeltaTransY = deltaTransY;
        final float currentScale = matrixValues[Matrix.MSCALE_X];
        transValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                float animatedTransX = transX + (finalDeltaTransX * animatedFraction);
                float animatedTransY = transY + (finalDeltaTransY * animatedFraction);
                mCurrentMatrix.setScale(currentScale, currentScale);
                mCurrentMatrix.postTranslate(animatedTransX, animatedTransY);

                ZoomableImageView.this.updateImageMatrix();
            }
        });
        transValueAnimator.addListener(new ScrollAnimatorListener());
        transValueAnimator.start();
    }

    //region Setters
    //FIXME: Prueba RegionDecoder
    public void setImageUri(Uri uri) {
        mImageUri = uri;

        try {
            // Establecer valor a las variables de dimensiones originales de la imagen.
            InputStream inputStream = getContext().getContentResolver().openInputStream(mImageUri);
            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;
            BitmapFactory.decodeStream(inputStream, null, onlyBoundsOptions);
            mOriginalImageHeight = onlyBoundsOptions.outHeight;
            mOriginalImageWidth = onlyBoundsOptions.outWidth;
            if (inputStream != null) {
                inputStream.close();
            }

            // Iniciar BitmapRegionDecoder.
            InputStream input = getContext().getContentResolver().openInputStream(mImageUri);
            mBitmapRegionDecoder = BitmapRegionDecoder.newInstance(input, false);
            if (input != null) {
                input.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Establecer valor a la variable de escalado original.
        mOriginalZoom = (float) mBitmapWidth / mOriginalImageWidth;
        mOriginalMatrix = new Matrix();
        mOriginalMatrix.setScale(mOriginalZoom, mOriginalZoom);
    }

    public void setScale(int displayWidth, int displayHeight) {
        if (mBitmapWidth != null) {
            return;
        }

        //FIXME: Prueba RegionDecoder
        mDisplayWidth = displayWidth;
        mDisplayHeight = displayHeight;


        ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            int availableMemoryInBytes = activityManager.getMemoryClass() * 1024 * 1024;
            Log.d(this.getClass().getName(), "Memoria Disponible para la Aplicación: " + availableMemoryInBytes);
            mImageTileCache = new ImageTileLruCache(availableMemoryInBytes / 8);
        }
        else {
            mImageTileCache = new ImageTileLruCache(6 * mDisplayHeight * mDisplayWidth);
        }

        // Establecer variables de tamaño de la imagen.
        Drawable drawable = this.getDrawable();
        mBitmapWidth = drawable.getIntrinsicWidth();
        mBitmapHeight = drawable.getIntrinsicHeight();

        // Escalar y centrar matriz de la view que contiene la imagen.
        RectF dest = new RectF(0, 0, displayWidth, displayHeight);
        RectF src = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
        mCurrentMatrix.setRectToRect(src, dest, Matrix.ScaleToFit.CENTER);
        mDefaultMatrix.set(mCurrentMatrix);

        // Establecer los valores para el factor de escala mínimo y máximo.
        setZoomValues();

        this.updateImageMatrix();
    }





    public static class AsyncImageRegionDecoder extends AsyncTask<Void, Void, Void> {
        // TODO: Verificar que esta task no rompe si se destruye la vista.

        private WeakReference<ZoomableImageView> zoomableImageViewWeakReference;

        AsyncImageRegionDecoder(ZoomableImageView zoomableImageViewWeakReference) {
            this.zoomableImageViewWeakReference = new WeakReference<>(zoomableImageViewWeakReference);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZoomableImageView zoomableImageView = zoomableImageViewWeakReference.get();
            if (zoomableImageView != null) {
                zoomableImageView.invalidate();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                ZoomableImageView zoomableImageView = zoomableImageViewWeakReference.get();
                if (zoomableImageView.mBitmapRegionDecoder == null) {
                    return null;
                }

                Set<ImageTile> tilesDraw = new HashSet<>(zoomableImageView.mTilesDraw);
                for (ImageTile imageTile : tilesDraw) {
                    if (isCancelled()) {
                        return null;
                    }
                    Bitmap bitmap = zoomableImageView.mImageTileCache.get(imageTile.getKey());
                    if (bitmap == null) {
                        Rect rect = new Rect((int) imageTile.mTileRect.left, (int) imageTile.mTileRect.top, (int) imageTile.mTileRect.right, (int) imageTile.mTileRect.bottom);
                        bitmap = zoomableImageView.mBitmapRegionDecoder.decodeRegion(rect, zoomableImageView.mBitmapOptions);
                        zoomableImageView.mImageTileCache.put(imageTile.getKey(), bitmap);
                        publishProgress();
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
            /*
            try {
                ZoomableImageView zoomableImageView = zoomableImageViewWeakReference.get();

                zoomableImageView.mTiles.clear();

                InputStream input = zoomableImageView.getContext().getContentResolver()
                    .openInputStream(zoomableImageView.mImageUri);
                BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder
                    .newInstance(input, false);

                if (zoomableImageView.mOriginalZoom == 0f) {
                    int originalWidth = bitmapRegionDecoder.getWidth();
                    zoomableImageView.mOriginalZoom = originalWidth / (float) zoomableImageView.mBitmapWidth;
                }

                Matrix matrix = new Matrix();
                matrix.setScale(1/zoomableImageView.mOriginalZoom, 1/zoomableImageView.mOriginalZoom);

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inPreferQualityOverSpeed = true;

                float[] matrixValues = new float[9];
                zoomableImageView.mCurrentMatrix.getValues(matrixValues);
                float offsetX = matrixValues[Matrix.MTRANS_X];
                float offsetY = matrixValues[Matrix.MTRANS_Y];
                float currentScale = matrixValues[Matrix.MSCALE_X];

                float regionStartX = -offsetX / currentScale * zoomableImageView.mOriginalZoom;
                float regionStartY = -offsetY / currentScale * zoomableImageView.mOriginalZoom;

                ///////////////////////
                // Determinar tiles necesarias:
                int firstTileIndexX = (int) regionStartX / TILE_WIDTH;
                int firstTileIndexY = (int) regionStartY / TILE_WIDTH;
                int regionWidth = (int) (zoomableImageView.mDisplayWidth / currentScale * zoomableImageView.mOriginalZoom);
                int regionHeight = (int) (zoomableImageView.mDisplayHeight / currentScale * zoomableImageView.mOriginalZoom);

                int lastTileIndexX = (int) (regionStartX + regionWidth) / TILE_WIDTH;
                int lastTileIndexY = (int) (regionStartY + regionHeight) / TILE_WIDTH;

                for(int i = firstTileIndexX; i <= lastTileIndexX; i++) {
                    for(int j = firstTileIndexY; j <= lastTileIndexY; j++) {
                        RectF region = new RectF(i * TILE_WIDTH, j * TILE_WIDTH,
                            i * TILE_WIDTH + TILE_WIDTH, j * TILE_WIDTH + TILE_WIDTH);
                        matrix.mapRect(region);
                        ImageTile imageTile = new ImageTile(region);
                        zoomableImageView.mTiles.add(imageTile);
                        publishProgress();
                    }
                }




//                Rect region = new Rect((int)regionStartX, (int)regionStartY,
//                    (int) regionStartX + TILE_WIDTH, (int) regionStartY + TILE_WIDTH);
//
////                Bitmap imageRegion = bitmapRegionDecoder.decodeRegion(region, options);
//
//                RectF rectF = new RectF(region);
//                matrix.mapRect(rectF);
//
//                ImageTile imageTile = new ImageTile(rectF, regionStartX, regionStartY);
//                if (zoomableImageView.mTiles.isEmpty()) {
//                    zoomableImageView.mTiles.add(imageTile);
//                }


                if (input != null) {
                    input.close();
                }



//
//                Rect region = new Rect(regionStartX, regionStartY,
//                    regionStartX + TILE_WIDTH, regionStartY + TILE_WIDTH);
//
//                Bitmap imageRegion = bitmapRegionDecoder.decodeRegion(region, options);
////                imageRegion = Bitmap.createScaledBitmap(imageRegion, mScreenSize.x, mScreenSize.y,
////                    false);
//
//
//                RectF rectF = new RectF(region);
//                matrix.mapRect(rectF);
////                mRegion.set(rectF);
//
//                ImageTile imageTile = new ImageTile(rectF, regionStartX, regionStartY);
//                if (zoomableImageView.mTiles.isEmpty()) {
//                    zoomableImageView.mTiles.add(imageTile);
//                }
//
//
//                if (input != null) {
//                    input.close();
//                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;*/
        }
    }


//    private void ComputeVisibleTiles() {
//
//        Matrix matrix = new Matrix();
//        matrix.setScale(mOriginalZoom, mOriginalZoom);
//
//        float[] matrixValues = new float[9];
//        mCurrentMatrix.getValues(matrixValues);
//
//        float offsetX = matrixValues[Matrix.MTRANS_X];
//        float offsetY = matrixValues[Matrix.MTRANS_Y];
//        float currentScale = matrixValues[Matrix.MSCALE_X];
//
////        float regionStartX = -offsetX / currentScale / mOriginalZoom;
//        float regionStartX = Math.max(0, -offsetX / currentScale / mOriginalZoom);
////        float regionStartY = -offsetY / currentScale / mOriginalZoom;
//        float regionStartY = Math.max(0, -offsetY / currentScale / mOriginalZoom);
//
//        int firstTileIndexX = (int) regionStartX / TILE_WIDTH;
//        int firstTileIndexY = (int) regionStartY / TILE_WIDTH;
//        int regionWidth = (int) (mDisplayWidth / currentScale / mOriginalZoom);
//        int regionHeight = (int) (mDisplayHeight / currentScale / mOriginalZoom);
//
////        int lastTileIndexX = (int) (regionStartX + regionWidth) / TILE_WIDTH;
//        int lastTileIndexX = (int) Math.min(mOriginalImageWidth / TILE_WIDTH, (regionStartX + regionWidth) / TILE_WIDTH);
////        int lastTileIndexY = (int) (regionStartY + regionHeight) / TILE_WIDTH;
//        int lastTileIndexY = (int) Math.min(mOriginalImageHeight / TILE_WIDTH, (regionStartY + regionHeight) / TILE_WIDTH);
//
//        // Calcular Tiles visibles.
//        List<ImageTile> visibleTiles = new ArrayList<>();
//        for(int rowIndex = firstTileIndexX; rowIndex <= lastTileIndexX; rowIndex++) {
//            for(int columnIndex = firstTileIndexY; columnIndex <= lastTileIndexY; columnIndex++) {
//                // Región correspondiente a la Tile.
//                RectF region = new RectF(rowIndex * TILE_WIDTH, columnIndex * TILE_WIDTH,
//                    (rowIndex + 1) * TILE_WIDTH, (columnIndex + 1) * TILE_WIDTH);
//
//                // Mapear según escalado original de Glide.
//                matrix.mapRect(region);
//
//                ImageTile imageTile = new ImageTile(region);
//
//                visibleTiles.add(imageTile);
//            }
//        }
//
//        // Eliminar de la lista actual de Tiles, aquellas que ya no son visibles.
////        Set<ImageTile> imageTiles = new HashSet<>(mTiles);
//        synchronized (mTilesDraw) {
//            mTilesDraw.retainAll(visibleTiles);
//            mTilesDraw.addAll(visibleTiles);
//        }
//    }

    public static class AsyncComputeVisibleTiles extends AsyncTask<Void, Void, Void> {
        private WeakReference<ZoomableImageView> zoomableImageViewReference;

        public AsyncComputeVisibleTiles(ZoomableImageView zoomableImageView) {
            this.zoomableImageViewReference = new WeakReference<>(zoomableImageView);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ZoomableImageView zoomableImageView = zoomableImageViewReference.get();
            if (zoomableImageView != null) {
                zoomableImageView.loadTiles();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ZoomableImageView zoomableImageView = zoomableImageViewReference.get();

            if (zoomableImageView == null) {
                return null;
            }

            Matrix matrix = new Matrix();
            matrix.setScale(zoomableImageView.mOriginalZoom, zoomableImageView.mOriginalZoom);

            float[] matrixValues = new float[9];
            zoomableImageView.mCurrentMatrix.getValues(matrixValues);

            float offsetX = matrixValues[Matrix.MTRANS_X];
            float offsetY = matrixValues[Matrix.MTRANS_Y];
            float currentScale = matrixValues[Matrix.MSCALE_X];

//        float regionStartX = -offsetX / currentScale / mOriginalZoom;
            float regionStartX = Math.max(0, -offsetX / currentScale / zoomableImageView.mOriginalZoom);
//        float regionStartY = -offsetY / currentScale / mOriginalZoom;
            float regionStartY = Math.max(0, -offsetY / currentScale / zoomableImageView.mOriginalZoom);

            int firstTileIndexX = (int) regionStartX / TILE_WIDTH;
            int firstTileIndexY = (int) regionStartY / TILE_WIDTH;
            int regionWidth = (int) (zoomableImageView.mDisplayWidth / currentScale / zoomableImageView.mOriginalZoom);
            int regionHeight = (int) (zoomableImageView.mDisplayHeight / currentScale / zoomableImageView.mOriginalZoom);

//        int lastTileIndexX = (int) (regionStartX + regionWidth) / TILE_WIDTH;
            int lastTileIndexX = (int) Math.min(zoomableImageView.mOriginalImageWidth / TILE_WIDTH, (regionStartX + regionWidth) / TILE_WIDTH);
//        int lastTileIndexY = (int) (regionStartY + regionHeight) / TILE_WIDTH;
            int lastTileIndexY = (int) Math.min(zoomableImageView.mOriginalImageHeight / TILE_WIDTH, (regionStartY + regionHeight) / TILE_WIDTH);

            // Calcular Tiles visibles.
            List<ImageTile> visibleTiles = new ArrayList<>();
            for(int rowIndex = firstTileIndexX; rowIndex <= lastTileIndexX; rowIndex++) {
                for(int columnIndex = firstTileIndexY; columnIndex <= lastTileIndexY; columnIndex++) {
                    if (isCancelled()) {
                        return null;
                    }

                    // Región correspondiente a la Tile.
                    RectF region = new RectF(rowIndex * TILE_WIDTH, columnIndex * TILE_WIDTH,
                        (rowIndex + 1) * TILE_WIDTH, (columnIndex + 1) * TILE_WIDTH);

                    // Mapear según escalado original de Glide.
//                    matrix.mapRect(region);

                    ImageTile imageTile = new ImageTile(region);

                    visibleTiles.add(imageTile);
                }
            }

            // Eliminar de la lista actual de Tiles, aquellas que ya no son visibles.
//        Set<ImageTile> imageTiles = new HashSet<>(mTiles);
            if (isCancelled()) {
                return null;
            }
            synchronized (zoomableImageView.mTilesDraw) {
                zoomableImageView.mTilesDraw.retainAll(visibleTiles);
                zoomableImageView.mTilesDraw.addAll(visibleTiles);
            }

            return null;
        }
    }

    private void loadTiles() {
        if (mAsyncImageRegionDecoder != null) {
            mAsyncImageRegionDecoder.cancel(true);
        }

        mAsyncImageRegionDecoder = new AsyncImageRegionDecoder(this);
        mAsyncImageRegionDecoder.execute();
    }


    private void setZoomValues() {
        float[] matrixValues = new float[9];
        mDefaultMatrix.getValues(matrixValues);

        // El factor de escala mínimo será el que se setea por defecto al escalar la imagen a la vista.
        mMinScaleFactor = matrixValues[Matrix.MSCALE_X];

        // El factor de escala máximo será el resultado de multiplicar el factor de escala mínimo por el
        // factor de escala de zoom máximo configurado.
        mMaxScaleFactor = mMinScaleFactor * MAX_ZOOM_SCALE;
    }

    private void initializeMembers() {
        // Establecer tipo de escala de la vista a Matriz.
        this.setScaleType(ScaleType.MATRIX);
        mDefaultMatrix = new Matrix();
        mCurrentMatrix = new Matrix();

        // Inicializar detectores de gestos y escala.
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ZoomableImageViewScaleListener());
        mGestureDetector = new GestureDetector(getContext(), new ZoomableImageViewGestureListener());

        // Inicializar objeto Paint para los dibujos sobre la imagen.
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(1f);
        mPaint.setAlpha(180);
//        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        // FIXME: Prueba RegionDecoder.
        mAlphaPaint = new Paint();
        mAlphaPaint.setAlpha(180);

        mBitmapOptions = new BitmapFactory.Options();
        mBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        mBitmapOptions.inPreferQualityOverSpeed = true;
    }
    //endregion

    private class ZoomableImageViewGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            if (!mCurrentMatrix.equals(mDefaultMatrix)) {
                // La imagen debe volver a su estado original (mDefaultMatrix).
                // Obtener valores finales (default) de la animación.
                float[] defaultMatrixValues = new float[9];
                mDefaultMatrix.getValues(defaultMatrixValues);
                float finalScaleFactor = defaultMatrixValues[Matrix.MSCALE_X];
                float finalTransX = defaultMatrixValues[Matrix.MTRANS_X];
                float finalTransY = defaultMatrixValues[Matrix.MTRANS_Y];

                // Obtener valores iniciales (actuales) de la animación.
                float[] currentMatrixValues = new float[9];
                mCurrentMatrix.getValues(currentMatrixValues);
                float currentScaleFactor = currentMatrixValues[Matrix.MSCALE_X];
                float currentTransX = currentMatrixValues[Matrix.MTRANS_X];
                float currentTransY = currentMatrixValues[Matrix.MTRANS_Y];

                // Animar - ValueAnimator
                mCurrentMatrix.reset();
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator.setDuration(ANIMATION_DURATION);
                valueAnimator.addUpdateListener(new ZoomAnimatorUpdateListener(currentScaleFactor,
                    currentTransX, currentTransY, finalScaleFactor, finalTransX, finalTransY));
                valueAnimator.addListener(new ZoomAnimatorListener(true));
                valueAnimator.start();
            }
            else {
//                float scale = mMinScaleFactor * MAX_ZOOM_SCALE / 6;
                float scale = MAX_ZOOM_SCALE / 2;
                final float currentScale = mMinScaleFactor;
                final float deltaScale = scale - currentScale;

                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator.setDuration(ANIMATION_DURATION);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    private float mLastScaleFactor = currentScale;
                    private final PointF pivot = new PointF(e.getX(),// getWidth() / 2,
                        e.getY());// getHeight() / 2);

                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float animatedFraction = valueAnimator.getAnimatedFraction();
                        float currentScaleFactor = currentScale + deltaScale * animatedFraction;
                        float deltaScaleFactor = currentScaleFactor / mLastScaleFactor;
                        mLastScaleFactor = mLastScaleFactor * deltaScaleFactor;

                        mCurrentMatrix.postScale(deltaScaleFactor, deltaScaleFactor, pivot.x, pivot.y);
                        ZoomableImageView.this.updateImageMatrix();
                    }
                });
                valueAnimator.addListener(new ZoomAnimatorListener(false));
                valueAnimator.start();
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Establecer estado de scroll.
            setState(States.Scrolling);

            // Desplazar matriz. Las distancias se invierten para que sean correctas.
            mCurrentMatrix.postTranslate(-distanceX, -distanceY);

            return true;
        }
    }

    private class ZoomableImageViewScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();

            // Obtener factor de escala actual.
            float[] matrixValues = new float[9];
            mCurrentMatrix.getValues(matrixValues);
            float currentScaleFactor = matrixValues[Matrix.MSCALE_X];

            // Forzar que el factor de escala sea menor o igual al factor de escala máximo.
            if (currentScaleFactor * scaleFactor > mMaxScaleFactor) {
                scaleFactor = mMaxScaleFactor / currentScaleFactor;
            }


            PointF focus = new PointF(detector.getFocusX(), detector.getFocusY());
            // Escalar matriz.
            mCurrentMatrix.postScale(scaleFactor, scaleFactor, focus.x, focus.y);

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            // FIXME: Si se hace zoom- y se scrollea al mismo tiempo, la imagen queda chica.
            // Verificar que el factor de escala final se encuentre dentro del rango permitido.
            float[] initialMatrixValues = new float[9];
            mCurrentMatrix.getValues(initialMatrixValues);
            final float initialScaleFactor = initialMatrixValues[Matrix.MSCALE_X];

            // Si el factor de escala es mayor al mínimo establecido, no llevar a cabo ninguna acción.
            if (initialScaleFactor > mMinScaleFactor) {
                return;
            }

            // Determinar valores iniciales y finales para la animación.
            float initialTransX = initialMatrixValues[Matrix.MTRANS_X];
            float initialTransY = initialMatrixValues[Matrix.MTRANS_Y];

            float[] defaultMatrixValues = new float[9];
            mDefaultMatrix.getValues(defaultMatrixValues);
            float finalScaleFactor = mMinScaleFactor;
            float finalTransX = defaultMatrixValues[Matrix.MTRANS_X];
            float finalTransY = defaultMatrixValues[Matrix.MTRANS_Y];

            // Animar - ValueAnimator
            mCurrentMatrix.reset();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.setDuration(ANIMATION_DURATION);
            valueAnimator.addUpdateListener(new ZoomAnimatorUpdateListener(initialScaleFactor, initialTransX,
                initialTransY, finalScaleFactor, finalTransX, finalTransY));
            valueAnimator.addListener(new ZoomAnimatorListener(false));
            valueAnimator.start();
        }
    }

    public class ZoomAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private float mInitialScaleFactor;
        private float mInitialTransX;
        private float mInitialTransY;
        private float mDeltaScaleFactor;
        private float mDeltaTransX;
        private float mDeltaTransY;

        ZoomAnimatorUpdateListener(float initialScaleFactor, float initialTransX, float initialTransY,
                                   float finalScaleFactor, float finalTransX, float finalTransY) {
            this.mInitialScaleFactor = initialScaleFactor;
            this.mInitialTransX = initialTransX;
            this.mInitialTransY = initialTransY;
            this.mDeltaScaleFactor = finalScaleFactor - initialScaleFactor;
            this.mDeltaTransX = finalTransX - initialTransX;
            this.mDeltaTransY = finalTransY - initialTransY;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float animatedFraction = valueAnimator.getAnimatedFraction();

            float animationScaleFactor = mInitialScaleFactor + mDeltaScaleFactor * animatedFraction;
            float animationTransX = mInitialTransX + mDeltaTransX * animatedFraction;
            float animationTransY = mInitialTransY + mDeltaTransY * animatedFraction;

            mCurrentMatrix.setScale(animationScaleFactor, animationScaleFactor);
            mCurrentMatrix.postTranslate(animationTransX, animationTransY);

            ZoomableImageView.this.updateImageMatrix();
        }
    }

    public class ZoomAnimatorListener implements ValueAnimator.AnimatorListener {
        private boolean setDefaultMatrix;

        public ZoomAnimatorListener(boolean setDefaultMatrix) {
            this.setDefaultMatrix = setDefaultMatrix;
        }

        @Override
        public void onAnimationStart(Animator animator) {
            setState(States.Animating);
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            setState(States.None);
            if (setDefaultMatrix) {
                mCurrentMatrix.set(mDefaultMatrix);
            }
            updateImageMatrix();
        }

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }

    public class ScrollAnimatorListener implements ValueAnimator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animator) {
            setState(States.Animating);
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            setState(States.None);
            updateImageMatrix();
        }

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }
}