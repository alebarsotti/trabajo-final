package barsotti.alejandro.prototipotf.customViews;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
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

public class ZoomableImageView extends android.support.v7.widget.AppCompatImageView {

    // region Constantes

    // Mínimo factor de escala permitido para la matriz de la View.
    private static final int MIN_SCALE_FACTOR = 1;
    // Máximo factor de escala permitido para la matriz de la View.
    private static final int MAX_SCALE_FACTOR = 30;
    // Duración de las animaciones (desplazamiento, zoom) en milisegundos.
    private static final int ANIMATION_DURATION = 250;
    // Tolerancia de desplazamiento para el dibujo de un nuevo punto en un Path en el modo de dibujo.
    private static final float TOUCH_TOLERANCE = 10;

    // endregion

    // region Variables

    // Ancho de cada Tile en píxeles.
    private int mTileSize = 256;
    // Ancho y alto en píxeles de la imagen escalada por Glide inicialmente.
    private Integer mBitmapWidth;
    private Integer mBitmapHeight;
    // Matriz original que centra la imagen en la pantalla.
    private Matrix mDefaultMatrix = new Matrix();
    // Matriz actual de la View, que refleja desplazamientos y escalado.
    private Matrix mCurrentMatrix = new Matrix();
    // Detector de gestos de escalado.
    private ScaleGestureDetector mScaleGestureDetector =
        new ScaleGestureDetector(getContext(), new ZoomableImageViewScaleListener());
    // Detector de gestos adicionales.
    private GestureDetector mGestureDetector =
        new GestureDetector(getContext(), new ZoomableImageViewGestureListener());
    // Estado actual de la vista.
    private States mState = States.None;
    // Variable utilizada para obtener los valores actuales de una matriz.
    private float[] mMatrixValues = new float[9];
    // Ancho y alto en píxeles de la pantalla del dispositivo.
    private Integer mDisplayWidth;
    private Integer mDisplayHeight;
    // Tarea asíncrona para cargar Tiles.
    private AsyncImageRegionDecoder mAsyncImageRegionDecoder;
    // TODO: Ver si se usa finalmente.
    // Tarea asíncrona para calcular Tiles visibles.
//    private AsyncComputeVisibleTiles mAsyncComputeVisibleTiles;
    // Factor de escala aplicado por Glide al cargar la imagen inicialmente.
    private float mOriginalZoom;
    // Lista de Tiles a dibujar.
    private final Set<ImageTile> mTilesDraw = new HashSet<>();
    // Caché que almacena Tiles decodificadas para optimizar su reutilización.
    private ImageTileLruCache mImageTileCache;
    // Ancho y alto en píxeles de la imagen original.
    private Integer mOriginalImageWidth = 0;
    private Integer mOriginalImageHeight = 0;
    // Matriz utilizada para escalar el Canvas previo al dibujo de las Tiles.
    private Matrix mCanvasMatrix = new Matrix();
    // BitmapRegionDecoder utilizado para decodificar Tiles de la imagen original.
    private BitmapRegionDecoder mBitmapRegionDecoder;
    // Opciones utilizadas para especificar cómo decodificar Tiles.
    private BitmapFactory.Options mBitmapOptions;

    // TODO: Revisar variables de dibujo de trazo.
    private Path mPath;
    private float mX;
    private float mY;

    // endregion

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

    private void updateImageMatrix(boolean computeVisibleTiles) {
        // Actualizar matriz utilizada en onDraw.
        mCanvasMatrix.setScale(mOriginalZoom, mOriginalZoom);
        mCanvasMatrix.postConcat(mCurrentMatrix);

        // Calcular Tiles visibles según la nueva matriz.
        if (computeVisibleTiles) {
            ComputeVisibleTiles();
        }

        // Actualizar matrix de la View.
        this.setImageMatrix(mCurrentMatrix);
    }

    private void ComputeVisibleTiles() {
        // Obtener valores de la matriz actual.
        mCurrentMatrix.getValues(mMatrixValues);
        float offsetX = mMatrixValues[Matrix.MTRANS_X];
        float offsetY = mMatrixValues[Matrix.MTRANS_Y];
        float currentScale = mMatrixValues[Matrix.MSCALE_X];

        // Calcular coordenadas que representan el inicio de la region visible en la imagen original.
        float regionStartX = Math.max(0, -offsetX / currentScale / mOriginalZoom);
        float regionStartY = Math.max(0, -offsetY / currentScale / mOriginalZoom);

        // Calcular dimensiones de la región visible actualmente.
        int regionWidth = (int) (mDisplayWidth / currentScale / mOriginalZoom);
        int regionHeight = (int) (mDisplayHeight / currentScale / mOriginalZoom);

        // Calcular índices de la primera Tile visible actualmente.
        int firstTileIndexX = (int) regionStartX / mTileSize;
        int firstTileIndexY = (int) regionStartY / mTileSize;

        // Calcular índices de la última Tile visible actualmente.
        int lastTileIndexX = (int) Math.min(mOriginalImageWidth / mTileSize,
            (regionStartX + regionWidth) / mTileSize);
        int lastTileIndexY = (int) Math.min(mOriginalImageHeight / mTileSize,
            (regionStartY + regionHeight) / mTileSize);

        // Determinar el SampleLevel adecuado para el factor de escala actual.
        int sampleLevel = 1;
        while (regionWidth / sampleLevel > mDisplayWidth ||
            regionHeight / sampleLevel > mDisplayHeight) {
            sampleLevel <<= 1;
        }
        mBitmapOptions.inSampleSize = sampleLevel;

        // Calcular Tiles visibles actualmente.
        List<ImageTile> visibleTiles = new ArrayList<>();
        for(int rowIndex = firstTileIndexX; rowIndex <= lastTileIndexX; rowIndex++) {
            for(int columnIndex = firstTileIndexY; columnIndex <= lastTileIndexY; columnIndex++) {

                // Región correspondiente a la Tile.
                RectF region = new RectF(rowIndex * mTileSize, columnIndex * mTileSize,
                    (rowIndex + 1) * mTileSize, (columnIndex + 1) * mTileSize);

                visibleTiles.add(new ImageTile(region, sampleLevel));
            }
        }

        // Actualizar lista de Tiles actual.
        mTilesDraw.clear();
        mTilesDraw.addAll(visibleTiles);

        // Forzar redibujo.
        invalidate();

        // Iniciar la carga de las Tiles visibles actualmente.
        loadVisibleTiles();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.performClick();

        // Verificar modo actual.
        if (!mState.equals(States.Drawing)) {
            // TODO: Verificar si es necesario realizar esto.
            // Matriz que almacena el último estado de la matriz actual.
            Matrix mLastMatrix = new Matrix(mCurrentMatrix);

            // No se está en modo Dibujo, informar del evento a los detectores de gestos y escala.
            mScaleGestureDetector.onTouchEvent(event);
            mGestureDetector.onTouchEvent(event);

            // Verificar necesidad de desplazamiento al finalizar un scroll.
            if (event.getAction() == MotionEvent.ACTION_UP && mState.equals(States.Scrolling)) {
                // Verificar que no se vean zonas fuera de la imagen. De verse, ajustar el desplazamiento.
                handleScrollEnded();
            }

            if (!mLastMatrix.equals(mCurrentMatrix)) {
                updateImageMatrix(false);
            }
        }
        else {
            // TODO: Revisar todo este branch.
            // Determinar en qué estado del dibujo se está.
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    if (mPath == null) {
                        mPath = new Path();
                    }
                    mPath.reset();
                    float x = event.getX();
                    float y = event.getY();
                    mPath.moveTo(x, y);
                    mX = x;
                    mY = y;
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    float x = event.getX();
                    float y = event.getY();
                    float dx = Math.abs(x - mX);
                    float dy = Math.abs(y - mY);
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                        mX = x;
                        mY = y;
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    mPath.lineTo(mX, mY);
                    Path mRealPath = new Path(mPath);
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

        // Guardar estado actual del Canvas y aplicar matriz de dibujo.
        canvas.save();
        canvas.setMatrix(mCanvasMatrix);
        // Dibujar los bitmaps de cada Tile.
        Bitmap mDrawBitmap;
        for (ImageTile imageTile: mTilesDraw) {
            mDrawBitmap = mImageTileCache.get(imageTile.Key);
            if (mDrawBitmap != null) {
                canvas.drawBitmap(mDrawBitmap, null, imageTile.Rect, null);
            }
        }
        // Restaurar el Canvas a su estado original.
        canvas.restore();
//        mDrawBitmap = null;
    }

    /**
     * Método ejecutado al finalizar un movimiento de scroll. Asegura que la imagen se vea correctamente.
     */
    private void handleScrollEnded() {
        // Cambiar estado actual, ya que se detuvo el scroll.
        setState(States.None);

        // Obtener valores de la matriz actual.
        mCurrentMatrix.getValues(mMatrixValues);
        // Si el factor de escala es menor al mínimo, no realizar ninguna acción.
        if (mMatrixValues[Matrix.MSCALE_X] < MIN_SCALE_FACTOR) {
            return;
        }
        final float transX = mMatrixValues[Matrix.MTRANS_X];
        final float transY = mMatrixValues[Matrix.MTRANS_Y];

        // Calcular dimensiones actuales de la imagen.
        float currentImageWidth = mBitmapWidth * mMatrixValues[Matrix.MSCALE_X];
        float currentImageHeight = mBitmapHeight * mMatrixValues[Matrix.MSCALE_Y];

        // Calcular límites de desplazamiento en X e Y.
        float minTransX = Math.min(mDisplayWidth - currentImageWidth, 0);
        float maxTransX = Math.max(mDisplayWidth - currentImageWidth, 0);

        float minTransY = Math.min(mDisplayHeight - currentImageHeight, 0);
        float maxTransY = Math.max(mDisplayHeight - currentImageHeight, 0);

        // Calcular delta X necesario para colocar la imagen nuevamente dentro de los límites permitidos.
        float deltaTransX = 0;
        if (transX > maxTransX || transX < minTransX) {
            float limitX = transX > maxTransX ? maxTransX : minTransX;
            deltaTransX = limitX - transX;
        }
        // Centrar horizontalmente en caso de que el ancho de la imagen sea menor al ancho de la pantalla.
        if (mDisplayWidth > currentImageWidth) {
            deltaTransX = (mDisplayWidth - currentImageWidth) / 2 - transX;
        }

        // Calcular delta Y necesario para colocar la imagen nuevamente dentro de los límites permitidos.
        float deltaTransY = 0;
        if (transY > maxTransY || transY < minTransY) {
            float limitY = transY > maxTransY ? maxTransY : minTransY;
            deltaTransY = limitY - transY;
        }
        // Si la altura de la imagen es menor a la altura de la pantalla, centrar verticalmente.
        if (mDisplayHeight > currentImageHeight) {
            deltaTransY = (mDisplayHeight - currentImageHeight) / 2 - transY;
        }

        // Si no es necesario desplazar la matriz, no realizar ninguna acción.
        if (deltaTransX == 0 && deltaTransY == 0) {
            // TODO: Verificar que sea necesario actualizar la matriz de la View y calcular Tiles.
            updateImageMatrix(true);
            return;
        }

        // Animar el desplazamiento de la matriz.
        ValueAnimator transValueAnimator = ValueAnimator.ofFloat(0, 1);
        transValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        transValueAnimator.setDuration(ANIMATION_DURATION);
        final float finalDeltaTransX = deltaTransX;
        final float finalDeltaTransY = deltaTransY;
        final float currentScale = mMatrixValues[Matrix.MSCALE_X];
        transValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                float animatedTransX = transX + (finalDeltaTransX * animatedFraction);
                float animatedTransY = transY + (finalDeltaTransY * animatedFraction);
                mCurrentMatrix.setScale(currentScale, currentScale);
                mCurrentMatrix.postTranslate(animatedTransX, animatedTransY);
                ZoomableImageView.this.updateImageMatrix(false);
            }
        });
        transValueAnimator.addListener(new ScrollAnimatorListener());
        transValueAnimator.start();
    }

    //region Setters

    /**
     * Método que inicializa y configura la View para su utilización.
     * @param displayWidth Ancho de la pantalla del dispositivo en píxeles.
     * @param displayHeight Alto de la pantalla del dispositivo en píxeles.
     * @param uri Uri de la imagen original a mostrar.
     * @param drawable Imagen escalada generada por Glide.
     */
    public void setupZoomableImageView(int displayWidth, int displayHeight, Uri uri, Drawable drawable) {
        // Establecer recurso Drawable de la View.
        this.setImageDrawable(drawable);

        // Establecer variables de dimensiones de pantalla del dispositivo.
        mDisplayWidth = displayWidth;
        mDisplayHeight = displayHeight;

        // Establecer variables de tamaño de la imagen.
        mBitmapWidth = drawable.getIntrinsicWidth();
        mBitmapHeight = drawable.getIntrinsicHeight();

        try {
            // Establecer valor a las variables de dimensiones originales de la imagen.
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
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
            InputStream input = getContext().getContentResolver().openInputStream(uri);
            mBitmapRegionDecoder = BitmapRegionDecoder.newInstance(input, false);
            if (input != null) {
                input.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Establecer valor a la variable de escalado original.
        mOriginalZoom = (float) mBitmapWidth / mOriginalImageWidth;

        // Determinar ancho máximo de las Tiles visibles en un momento dado en escala real (1).
        int maxHeightVisibleTiles = (int) Math.ceil((mDisplayHeight / (double) mTileSize) + 1) * mTileSize;
        int maxWidthVisibleTiles = (int) Math.ceil((mDisplayWidth / (double) mTileSize) + 1) * mTileSize;

        // Calcular tamaño óptimo para la caché de Tiles: tamaño calculado o 1/8 de la memoria disponible.
        int computedCacheSize = 4 * maxHeightVisibleTiles * maxWidthVisibleTiles;
        ActivityManager activityManager = (ActivityManager) getContext()
            .getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            int availableCacheSize = activityManager.getMemoryClass() * 1024 * 1024 / 8;
            mImageTileCache = new ImageTileLruCache(Math.min(availableCacheSize, computedCacheSize));
        }
        else {
            mImageTileCache = new ImageTileLruCache(computedCacheSize);
        }

        // Centrar matriz de la View que contiene la imagen.
        RectF dest = new RectF(0, 0, displayWidth, displayHeight);
        RectF src = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
        mCurrentMatrix.setRectToRect(src, dest, Matrix.ScaleToFit.CENTER);
        mDefaultMatrix.set(mCurrentMatrix);
        updateImageMatrix(false);
    }

    /**
     * Método utilizado para lanzar la tarea asíncrona encargada de decodificar Tiles.
     */
    private void loadVisibleTiles() {
        if (mAsyncImageRegionDecoder != null) {
            mAsyncImageRegionDecoder.cancel(true);
        }

        mAsyncImageRegionDecoder = new AsyncImageRegionDecoder(this);
        mAsyncImageRegionDecoder.execute();
    }

    /**
     * Método que inicializa variables fundamentales de la View.
     */
    private void initializeMembers() {
        // Establecer tipo de escala de la vista a Matriz.
        this.setScaleType(ScaleType.MATRIX);

        // Establecer configuración para la decodificación de Bitmaps.
        mBitmapOptions = new BitmapFactory.Options();
        mBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        mBitmapOptions.inPreferQualityOverSpeed = true;
    }
    //endregion

    public static class AsyncImageRegionDecoder extends AsyncTask<Void, Void, Void> {
        // TODO: Verificar que esta task no rompe si se destruye la vista.

        private WeakReference<ZoomableImageView> zoomableImageViewWeakReference;

        AsyncImageRegionDecoder(ZoomableImageView zoomableImageViewWeakReference) {
            this.zoomableImageViewWeakReference = new WeakReference<>(zoomableImageViewWeakReference);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // Cada vez que se finaliza la decodificación de una Tile, requerir redibujo de la View.
            ZoomableImageView zoomableImageView = zoomableImageViewWeakReference.get();
            if (zoomableImageView != null) {
                zoomableImageView.invalidate();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                ZoomableImageView view = zoomableImageViewWeakReference.get();
                // Verificar que el BitmapRegionDecoder esté disponible.
                if (view.mBitmapRegionDecoder == null) {
                    return null;
                }

                // Copiar lista de Tiles a decodificar.
                Set<ImageTile> tilesDraw = new HashSet<>(view.mTilesDraw);
                for (ImageTile tile : tilesDraw) {
                    // Si la tarea asíncrona fue cancelada, abortar ejecución.
                    if (isCancelled()) {
                        return null;
                    }
                    // Buscar bitmap de la Tile en la memoria caché. De no encontrarse, generarlo.
                    Bitmap bitmap = view.mImageTileCache.get(tile.Key);
                    if (bitmap == null) {
                        Rect rect = new Rect((int) tile.Rect.left, (int) tile.Rect.top,
                            (int) tile.Rect.right, (int) tile.Rect.bottom);
                        bitmap = view.mBitmapRegionDecoder.decodeRegion(rect, view.mBitmapOptions);
                        view.mImageTileCache.put(tile.Key, bitmap);
                        publishProgress();
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

//    public static class AsyncComputeVisibleTiles extends AsyncTask<Void, Void, Void> {
//        private WeakReference<ZoomableImageView> zoomableImageViewReference;
//
//        AsyncComputeVisibleTiles(ZoomableImageView zoomableImageView) {
//            this.zoomableImageViewReference = new WeakReference<>(zoomableImageView);
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            ZoomableImageView zoomableImageView = zoomableImageViewReference.get();
//            if (zoomableImageView != null) {
//                zoomableImageView.loadVisibleTiles();
//            }
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            ZoomableImageView zoomableImageView = zoomableImageViewReference.get();
//
//            if (zoomableImageView == null) {
//                return null;
//            }
//
//            Matrix matrix = new Matrix();
//            matrix.setScale(zoomableImageView.mOriginalZoom, zoomableImageView.mOriginalZoom);
//
//            zoomableImageView.mCurrentMatrix.getValues(zoomableImageView.mMatrixValues);
//
//            float offsetX = zoomableImageView.mMatrixValues[Matrix.MTRANS_X];
//            float offsetY = zoomableImageView.mMatrixValues[Matrix.MTRANS_Y];
//            float currentScale = zoomableImageView.mMatrixValues[Matrix.MSCALE_X];
//
////        float regionStartX = -offsetX / currentScale / mOriginalZoom;
//            float regionStartX = Math.max(0, -offsetX / currentScale / zoomableImageView.mOriginalZoom);
////        float regionStartY = -offsetY / currentScale / mOriginalZoom;
//            float regionStartY = Math.max(0, -offsetY / currentScale / zoomableImageView.mOriginalZoom);
//
//            int firstTileIndexX = (int) regionStartX / mTileSize;
//            int firstTileIndexY = (int) regionStartY / mTileSize;
//            int regionWidth = (int) (zoomableImageView.mDisplayWidth / currentScale / zoomableImageView.mOriginalZoom);
//            int regionHeight = (int) (zoomableImageView.mDisplayHeight / currentScale / zoomableImageView.mOriginalZoom);
//
////        int lastTileIndexX = (int) (regionStartX + regionWidth) / mTileSize;
//            int lastTileIndexX = (int) Math.min(zoomableImageView.mOriginalImageWidth / mTileSize, (regionStartX + regionWidth) / mTileSize);
////        int lastTileIndexY = (int) (regionStartY + regionHeight) / mTileSize;
//            int lastTileIndexY = (int) Math.min(zoomableImageView.mOriginalImageHeight / mTileSize, (regionStartY + regionHeight) / mTileSize);
//
//            // Calcular Tiles visibles.
//            List<ImageTile> visibleTiles = new ArrayList<>();
//            for(int rowIndex = firstTileIndexX; rowIndex <= lastTileIndexX; rowIndex++) {
//                for(int columnIndex = firstTileIndexY; columnIndex <= lastTileIndexY; columnIndex++) {
//                    if (isCancelled()) {
//                        return null;
//                    }
//
//                    // Región correspondiente a la Tile.
//                    RectF region = new RectF(rowIndex * mTileSize, columnIndex * mTileSize,
//                        (rowIndex + 1) * mTileSize, (columnIndex + 1) * mTileSize);
//
//                    // Mapear según escalado original de Glide.
////                    matrix.mapRect(region);
//
//                    ImageTile imageTile = new ImageTile(region, 1);
//
//                    visibleTiles.add(imageTile);
//                }
//            }
//
//            // Eliminar de la lista actual de Tiles, aquellas que ya no son visibles.
////        Set<ImageTile> imageTiles = new HashSet<>(mTiles);
//            if (isCancelled()) {
//                return null;
//            }
//            synchronized (zoomableImageView.mTilesDraw) {
//                zoomableImageView.mTilesDraw.retainAll(visibleTiles);
//                zoomableImageView.mTilesDraw.addAll(visibleTiles);
//            }
//
//            return null;
//        }
//    }

    private class ZoomableImageViewGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            // Determinar acción a realizar (zoom in o zoom out).
            if (!mCurrentMatrix.equals(mDefaultMatrix)) {
                // Restablecer matriz a su estado original.

                // Obtener valores finales (default) de la matriz.
                mDefaultMatrix.getValues(mMatrixValues);
                float finalScaleFactor = mMatrixValues[Matrix.MSCALE_X];
                float finalTransX = mMatrixValues[Matrix.MTRANS_X];
                float finalTransY = mMatrixValues[Matrix.MTRANS_Y];

                // Obtener valores iniciales (actuales) de la matriz.
                mCurrentMatrix.getValues(mMatrixValues);
                float currentScaleFactor = mMatrixValues[Matrix.MSCALE_X];
                float currentTransX = mMatrixValues[Matrix.MTRANS_X];
                float currentTransY = mMatrixValues[Matrix.MTRANS_Y];

                // Animar transición.
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
                // Escalar hasta 1/3 del factor de escala máximo, con centro en las coordenadas del evento.
                final float deltaScale = MAX_SCALE_FACTOR / 3 - MIN_SCALE_FACTOR;

                // Animar transición.
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator.setDuration(ANIMATION_DURATION);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    private float mLastScaleFactor = MIN_SCALE_FACTOR;
                    private final PointF pivot = new PointF(e.getX(), e.getY());

                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float animatedFraction = valueAnimator.getAnimatedFraction();
                        float currentScaleFactor = MIN_SCALE_FACTOR + deltaScale * animatedFraction;
                        float deltaScaleFactor = currentScaleFactor / mLastScaleFactor;
                        mLastScaleFactor = mLastScaleFactor * deltaScaleFactor;

                        mCurrentMatrix.postScale(deltaScaleFactor, deltaScaleFactor, pivot.x, pivot.y);
                        ZoomableImageView.this.updateImageMatrix(false);
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

            // Desplazar matriz. Las distancias se deben invertir para ser correctas.
            mCurrentMatrix.postTranslate(-distanceX, -distanceY);

            return true;
        }
    }

    private class ZoomableImageViewScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Obtener factor de escala del gesto.
            float scaleFactor = detector.getScaleFactor();

            // Obtener factor de escala actual.
            mCurrentMatrix.getValues(mMatrixValues);
            float currentScaleFactor = mMatrixValues[Matrix.MSCALE_X];

            // Forzar que el factor de escala resultante sea menor o igual al factor de escala máximo.
            if (currentScaleFactor * scaleFactor > MAX_SCALE_FACTOR) {
                scaleFactor = MAX_SCALE_FACTOR / currentScaleFactor;
            }

            // Escalar matriz con centro en las coordenadas del evento.
            mCurrentMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            // Verificar que el factor de escala final se encuentre dentro del rango permitido.
            mCurrentMatrix.getValues(mMatrixValues);
            final float initialScaleFactor = mMatrixValues[Matrix.MSCALE_X];

            // Si el factor de escala es mayor al mínimo establecido, no llevar a cabo ninguna acción.
            if (initialScaleFactor > MIN_SCALE_FACTOR) {
                return;
            }

            // Determinar valores iniciales y finales para la animación.
            float initialTransX = mMatrixValues[Matrix.MTRANS_X];
            float initialTransY = mMatrixValues[Matrix.MTRANS_Y];

            mDefaultMatrix.getValues(mMatrixValues);
            float finalTransX = mMatrixValues[Matrix.MTRANS_X];
            float finalTransY = mMatrixValues[Matrix.MTRANS_Y];

            // Animar transición.
            mCurrentMatrix.reset();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.setDuration(ANIMATION_DURATION);
            valueAnimator.addUpdateListener(new ZoomAnimatorUpdateListener(initialScaleFactor, initialTransX,
                initialTransY, (float) MIN_SCALE_FACTOR, finalTransX, finalTransY));
            valueAnimator.addListener(new ZoomAnimatorListener(false));
            valueAnimator.start();
        }
    }

    //region Animaciones
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

            ZoomableImageView.this.updateImageMatrix(false);
        }
    }

    public class ZoomAnimatorListener implements ValueAnimator.AnimatorListener {
        private boolean setDefaultMatrix;

        ZoomAnimatorListener(boolean setDefaultMatrix) {
            this.setDefaultMatrix = setDefaultMatrix;
        }

        @Override
        public void onAnimationStart(Animator animator) {
            setState(States.Animating);
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            // Establecer estado None al finalizar la animación de zoom.
            setState(States.None);

            // Reemplazar la matriz actual por la matriz por defecto (double-tap zoom out) si es necesario.
            if (setDefaultMatrix) {
                mCurrentMatrix.set(mDefaultMatrix);
            }

            // Recalcular Tiles visibles.
            updateImageMatrix(true);
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
            // Establecer estado None y recalcular Tiles visibles al finalizar scroll.
            setState(States.None);
            updateImageMatrix(true);
        }

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }
    //endregion
}