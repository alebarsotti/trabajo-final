package barsotti.alejandro.trabajoFinal.customViews;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.OverScroller;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import barsotti.alejandro.trabajoFinal.R;
import barsotti.alejandro.trabajoFinal.customInterfaces.IOnMatrixViewChangeListener;
import barsotti.alejandro.trabajoFinal.customInterfaces.IShapeCreator;
import barsotti.alejandro.trabajoFinal.utils.ImageTile;
import barsotti.alejandro.trabajoFinal.utils.ImageTileLruCache;
import barsotti.alejandro.trabajoFinal.utils.ViewUtils;

public class ZoomableImageView extends android.support.v7.widget.AppCompatImageView
    implements IShapeCreator {

    // region Constantes
    /**
     * Tag utilizado con fines de debug.
     */
    private static final String TAG = "ZoomableImageView";
    /**
     * Mínimo factor de escala permitido para la matriz de la View.
     */
    private static final int MIN_SCALE_FACTOR = ViewUtils.MIN_SCALE_FACTOR;
    /**
     * Máximo factor de escala permitido para la matriz de la View.
     */
    private static final int MAX_SCALE_FACTOR = ViewUtils.MAX_SCALE_FACTOR;
    /**
     * Duración de las animaciones (desplazamiento, zoom) en milisegundos.
     */
    private static final int ANIMATION_DURATION = 250;
    /**
     * Dimensión inicial de cada Tile, expresada en píxeles.
     */
    private static final int INITIAL_TILE_SIZE = 64;
    /**
     * Dimensión máxima de cada Tile, expresada en píxeles.
     */
    private static final int MAX_TILE_SIZE = 1024;
    // endregion

    // region Propiedades
    /**
     * Dimensión de cada Tile, expresada en píxeles.
     */
    private int mTileSize = INITIAL_TILE_SIZE;
    /**
     * Factor de escala aplicado por Glide al cargar la imagen inicialmente.
     */
    private float mOriginalZoom;
    /**
     * Anchura de la imagen escalada por Glide inicialmente, expresada en píxeles.
     */
    private Integer mBitmapWidth;
    /**
     * Altura de la imagen escalada por Glide inicialmente, expresada en píxeles.
     */
    private Integer mBitmapHeight;
    /**
     * Matriz original que almacena los valores de desplazamiento y escalado utilizados para centrar la
     * imagen en la View.
     */
    private Matrix mDefaultMatrix = new Matrix();
    /**
     * Matriz actual de la View, que almacena los valores de desplazamiento y escalado actuales.
     */
    private Matrix mCurrentMatrix = new Matrix();
    /**
     * Detector de gestos utilizado para detectar escalado.
     */
    private ScaleGestureDetector mScaleGestureDetector =
        new ScaleGestureDetector(getContext(), new ZoomableImageViewScaleListener());
    /**
     * Detector de gestos utilizado para detectar gestos adicionales.
     */
    private GestureDetector mGestureDetector =
        new GestureDetector(getContext(), new ZoomableImageViewGestureListener());
    /**
     * Estado actual de la vista.
     */
    private ViewState mViewState = ViewState.None;
    /**
     * Variable auxiliar utilizada para obtener los valores actuales de una matriz dada.
     */
    private float[] mMatrixValues = new float[9];
    /**
     * Anchura de la pantalla del dispositivo, expresada en píxeles.
     */
    private Integer mDisplayWidth;
    /**
     * Altura de la pantalla del dispositivo, expresada en píxeles.
     */
    private Integer mDisplayHeight;
    /**
     * Tarea asíncrona personalizada, utilizada para cargar Tiles en memoria en segundo plano.
     */
    private AsyncImageRegionDecoder mAsyncImageRegionDecoder;
    /**
     * Lista de Tiles a dibujar en pantalla.
     */
    private final Set<ImageTile> mTilesDraw = new HashSet<>();
    /**
     * Caché que almacena Tiles decodificadas para optimizar su reutilización.
     */
    private ImageTileLruCache mImageTileCache;
    /**
     * Anchura de la imagen original, espresada en píxeles.
     */
    private Integer mOriginalImageWidth = 0;
    /**
     * Altura de la imagen original, espresada en píxeles.
     */
    private Integer mOriginalImageHeight = 0;
    /**
     * Matriz utilizada para escalar el Canvas, previo al dibujo de las Tiles.
     */
    private Matrix mCanvasMatrix = new Matrix();
    /**
     * BitmapRegionDecoder utilizado para decodificar Tiles de la imagen original.
     */
    private BitmapRegionDecoder mBitmapRegionDecoder;
    /**
     * Opciones utilizadas para especificar el modo de decodificación de las Tiles.
     */
    private BitmapFactory.Options mBitmapOptions;
    /**
     * Pintura con Anti-Alias utilizada para dibujado de Tiles en pantalla.
     */
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * Scroller utilizado para realizar el movimiento "fling".
     */
    private OverScroller mScroller = new OverScroller(getContext());
    /**
     * Variable que indica si se está realizando el dibujo de una figura actualmente.
     */
    private boolean mDrawingInProgress = false;
    /**
     * Lista de objetos suscritos al evento de actualización de la matriz de esta vista.
     */
    private ArrayList<IOnMatrixViewChangeListener> mListeners = new ArrayList<>();
    /**
     * Estados posibles de la View.
     */
    public enum ViewState {
        None,
        Scrolling,
        Animating,
        Flinging
    }
    // endregion

    //region Constructors
    public ZoomableImageView(Context context) {
        this(context, null);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.d(TAG, getContext().getString(R.string.constructor_log_message));

        // Establecer tipo de escala de la vista a Matriz.
        this.setScaleType(ScaleType.MATRIX);

        // Establecer configuración para la decodificación de Bitmaps.
        mBitmapOptions = new BitmapFactory.Options();
    }
    //endregion

    //region Adminitración de listeners
    public void addOnMatrixViewChangeListener(IOnMatrixViewChangeListener listener) {
        mListeners.add(listener);
        listener.setViewMeasures(getMeasuredWidth(), getMeasuredHeight());
        listener.updateViewMatrix(mCanvasMatrix);
    }

    public void removeOnMatrixViewChangeListener(IOnMatrixViewChangeListener listener) {
        try {
            mListeners.remove(listener);
        }
        catch (Exception e) {
            Log.d(TAG, "removeOnMatrixViewChangeListener: " +
                getContext().getString(R.string.listener_not_found_error));
        }
    }
    //endregion

    //region Setters
    /**
     * Establece el estado de la View.
     * @param viewState Nuevo valor para el estado de la View.
     */
    public void setState(ViewState viewState) {
        mViewState = viewState;
    }

    /**
     * Establece el estado de la variable que indica si se está dibujando actualmente.
     * @param drawingInProgress Nuevo valor para la variable de estado.
     */
    public void setDrawingInProgress(boolean drawingInProgress) {
        mDrawingInProgress = drawingInProgress;
    }

    /**
     * Inicializa y configura la View para su utilización.
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
    //endregion

    //region Getters
    @Override
    public float getOriginalZoom() {
        return mOriginalZoom;
    }
    //endregion

    //region Dibujo de figuras

    /**
     * Agrega un nuevo punto a la figura que se encuentra en proceso de dibujo.
     * @param point Punto a agregar a la figura.
     */
    public void addPointToShape(PointF point) {
        ZoomableImageViewGroup parent = (ZoomableImageViewGroup) this.getParent();
        parent.addPointToInProgressShape(point);
    }

    /**
     * Verifica si un toque seleccionó alguna figura dibujada.
     * @param point Coordenadas del toque en pantalla.
     */
    public void checkShapeSelection(PointF point) {
        ZoomableImageViewGroup parent = (ZoomableImageViewGroup) this.getParent();
        parent.checkShapeSelection(point);
    }
    //endregion

    //region Presentación
    /**
     * Actualiza la matriz de la View. Adicionalmente, recalcula la lista de Tiles visibles.
     * @param computeVisibleTiles Indica si se debe recalcular la lista de Tiles visibles.
     */
    private void updateImageMatrix(boolean computeVisibleTiles) {
        // Actualizar matriz utilizada en onDraw.
        mCanvasMatrix.setScale(mOriginalZoom, mOriginalZoom);
        mCanvasMatrix.postConcat(mCurrentMatrix);

        // Calcular Tiles visibles según la nueva matriz, según pedido.
        if (computeVisibleTiles) {
            //region Calcular Tiles visibles.
            // Obtener valores de la matriz actual.
            mCurrentMatrix.getValues(mMatrixValues);
            float offsetX = mMatrixValues[Matrix.MTRANS_X];
            float offsetY = mMatrixValues[Matrix.MTRANS_Y];
            float currentScale = mMatrixValues[Matrix.MSCALE_X];

            // Calcular coordenadas que representan el inicio de la region visible en la imagen original.
            float regionStartX = Math.max(0, -offsetX / currentScale / mOriginalZoom);
            float regionStartY = Math.max(0, -offsetY / currentScale / mOriginalZoom);

            // Calcular dimensiones de la región visible actualmente.
            float regionWidth = mDisplayWidth / currentScale / mOriginalZoom;
            float regionHeight = mDisplayHeight / currentScale / mOriginalZoom;

            // Calcular índices de Tiles visibles y tamaño de Tiles adecuado.
            int firstTileIndexX, firstTileIndexY, lastTileIndexX, lastTileIndexY;
            mTileSize = INITIAL_TILE_SIZE >> 1;
            do {
                mTileSize <<= 1;

                // Calcular índices de la primera Tile visible actualmente con el tamaño de Tile actual.
                firstTileIndexX = (int) regionStartX / mTileSize;
                firstTileIndexY = (int) regionStartY / mTileSize;

                // Calcular índices de la última Tile visible actualmente con el tamaño de Tile actual.
                lastTileIndexX = (int) Math.min(mOriginalImageWidth / mTileSize,
                    (regionStartX + regionWidth) / mTileSize);
                lastTileIndexY = (int) Math.min(mOriginalImageHeight / mTileSize,
                    (regionStartY + regionHeight) / mTileSize);
            } while (Math.max(lastTileIndexX - firstTileIndexX, lastTileIndexY - firstTileIndexY) + 1 > 9 &&
                mTileSize < MAX_TILE_SIZE);

            // Determinar el SampleLevel adecuado para el factor de escala actual y el tamaño de la caché.
            int sampleLevel = 1;
            int cacheMaxSize = mImageTileCache.maxSize();
            double totalBitmapBytes = 4 * // Cada pixel requiere 4 bytes.
                Math.ceil((regionWidth / (double) mTileSize) + 1) * // Tiles en X.
                Math.ceil((regionHeight / (double) mTileSize) + 1) * // Tiles en Y.
                Math.pow(mTileSize, 2); // Cantidad de píxeles de cada Tile.

            while (regionWidth / sampleLevel > mDisplayWidth ||
                regionHeight / sampleLevel > mDisplayHeight ||
                cacheMaxSize < (totalBitmapBytes / Math.pow(sampleLevel, 2))) {
                sampleLevel <<= 1;
            }
            mBitmapOptions.inSampleSize = sampleLevel;

            // Calcular Tiles visibles actualmente.
            List<ImageTile> visibleTiles = new ArrayList<>();
            for(int rowIndex = firstTileIndexX; rowIndex <= lastTileIndexX; rowIndex++) {
                for(int columnIndex = firstTileIndexY; columnIndex <= lastTileIndexY; columnIndex++) {

                    // Región correspondiente a la Tile.
                    Rect region = new Rect(rowIndex * mTileSize, columnIndex * mTileSize,
                        Math.min((rowIndex + 1) * mTileSize, mOriginalImageWidth),
                        Math.min((columnIndex + 1) * mTileSize, mOriginalImageHeight));
                    visibleTiles.add(new ImageTile(region, sampleLevel, mTileSize));
                }
            }

            // Actualizar lista de Tiles actual.
            mTilesDraw.clear();
            mTilesDraw.addAll(visibleTiles);

            // Forzar redibujo.
            invalidate();

            // Iniciar la carga de las Tiles visibles actualmente.
            //region Cargar tiles visibles
            if (mAsyncImageRegionDecoder != null) {
                mAsyncImageRegionDecoder.cancel(true);
            }

            mAsyncImageRegionDecoder = new AsyncImageRegionDecoder(this);
            mAsyncImageRegionDecoder.execute();
            //endregion
            //endregion
        }

        // Desencadenar evento de actualización de matriz para listeners.
        for (IOnMatrixViewChangeListener listener: mListeners) {
            listener.updateViewMatrix(mCanvasMatrix);
        }

        // Actualizar matrix de la View.
        this.setImageMatrix(mCurrentMatrix);
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
                canvas.drawBitmap(mDrawBitmap, null, imageTile.Rect, mPaint);
            }
        }

        // Restaurar el Canvas a su estado original.
        canvas.restore();
    }

    /**
     * Método ejecutado al finalizar un movimiento. Asegura que la imagen se vea correctamente.
     */
    private void verifyAndCorrectImagePosition() {
        // Cambiar estado actual, ya que se detuvo la acción realizada.
        setState(ViewState.None);

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
            // Actualizar la matriz de la View y calcular Tiles.
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

    /**
     * Clase utilizada para decodificar Tiles de la imagen y almacenarlas en memoria en segundo plano.
     */
    public static class AsyncImageRegionDecoder extends AsyncTask<Void, Void, Void> {
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
            else {
                // Cancelar si ya no se posee la referencia a la View.
                this.cancel(true);
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
                Bitmap bitmap;
                for (ImageTile tile : tilesDraw) {
                    // Si la tarea asíncrona fue cancelada, abortar ejecución.
                    if (isCancelled()) {
                        return null;
                    }
                    // Buscar bitmap de la Tile en la memoria caché. De no encontrarse, generarlo.
                    bitmap = view.mImageTileCache.get(tile.Key);
                    if (bitmap != null) {
                        return null;
                    }
                    bitmap = view.mBitmapRegionDecoder.decodeRegion(tile.Rect, view.mBitmapOptions);
                    view.mImageTileCache.put(tile.Key, bitmap);
                    publishProgress();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    //region Animaciones

    /**
     * Clase utilizada para controlar la animación de un evento de zoom.
     */
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

    /**
     * Clase que controla las acciones realizadas al inicio y al final de una animación de zoom.
     */
    public class ZoomAnimatorListener implements ValueAnimator.AnimatorListener {
        private boolean setDefaultMatrix;

        ZoomAnimatorListener(boolean setDefaultMatrix) {
            this.setDefaultMatrix = setDefaultMatrix;
        }

        @Override
        public void onAnimationStart(Animator animator) {
            setState(ViewState.Animating);
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            // Establecer estado None al finalizar la animación de zoom.
            setState(ViewState.None);

            // Reemplazar la matriz actual por la matriz por defecto (double-tap zoom out) si es necesario.
            if (setDefaultMatrix) {
                mCurrentMatrix.set(mDefaultMatrix);
            }
            else {
                verifyAndCorrectImagePosition();
            }

            // Recalcular Tiles visibles.
            updateImageMatrix(true);
        }

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }

    /**
     * Clase que controla las acciones realizadas al inicio y al final de una animación de scroll.
     */
    public class ScrollAnimatorListener implements ValueAnimator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animator) {
            setState(ViewState.Animating);
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            // Establecer estado None y recalcular Tiles visibles al finalizar scroll.
            setState(ViewState.None);
            updateImageMatrix(true);
        }

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }
    //endregion

    //endregion

    //region Procesamiento de toques y detección de gestos
    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.performClick();

        // Procesar evento para detectar posibles gestos.
        mScaleGestureDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);

        // Verificar necesidad de desplazamiento al finalizar un scroll.
        if (event.getAction() == MotionEvent.ACTION_UP && mViewState.equals(ViewState.Scrolling)) {
            // Verificar que no se vean zonas fuera de la imagen. De verse, ajustar el desplazamiento.
            verifyAndCorrectImagePosition();
        }

        updateImageMatrix(false);

        return true;
    }

    /**
     * Clase que controla las acciones llevadas a cabo al detectar gestos en pantalla.
     */
    private class ZoomableImageViewGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mDrawingInProgress) {
                mCurrentMatrix.getValues(mMatrixValues);

                float x = (-mMatrixValues[Matrix.MTRANS_X] + e.getX())
                    / mMatrixValues[Matrix.MSCALE_X] / mOriginalZoom;
                float y = (-mMatrixValues[Matrix.MTRANS_Y] + e.getY())
                    / mMatrixValues[Matrix.MSCALE_X] / mOriginalZoom;

                addPointToShape(new PointF(x, y));

                updateImageMatrix(false);
            }
            else {
                checkShapeSelection(new PointF(e.getX(), e.getY()));
            }

            return true;
        }

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
                final float deltaScale = MAX_SCALE_FACTOR / 3;

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
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Establecer estado de fling.
            setState(ViewState.Flinging);

            // Realizar el fling.
            fling(velocityX, velocityY);

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Establecer estado de scroll.
            setState(ViewState.Scrolling);

            // Desplazar matriz. Las distancias se deben invertir para ser correctas.
            mCurrentMatrix.postTranslate(-distanceX, -distanceY);

            return true;
        }
    }

    /**
     * Realiza la animación de una acción "fling".
     * @param velocityX Velocidad horizontal del fling.
     * @param velocityY Velocidad vertical del fling.
     */
    private void fling(float velocityX, float velocityY) {
        // Cancelar cualquier animación que pudiera estar en progreso.
        mScroller.forceFinished(true);

        // Obtener valores de la matriz actual.
        mCurrentMatrix.getValues(mMatrixValues);

        // Calcular dimensiones actuales de la imagen.
        float currentImageWidth = mBitmapWidth * mMatrixValues[Matrix.MSCALE_X];
        float currentImageHeight = mBitmapHeight * mMatrixValues[Matrix.MSCALE_Y];

        // Calcular límites de desplazamiento en X e Y.
        int minTransX = (int) Math.min(mDisplayWidth - currentImageWidth, 0);
        int maxTransX = (int) Math.max(mDisplayWidth - currentImageWidth, 0);
        int minTransY = (int) Math.min(mDisplayHeight - currentImageHeight, 0);
        int maxTransY = (int) Math.max(mDisplayHeight - currentImageHeight, 0);

        // Crear la animación de fling.
        mScroller.fling((int)mMatrixValues[Matrix.MTRANS_X], (int)mMatrixValues[Matrix.MTRANS_Y],
            (int)velocityX, (int)velocityY, minTransX, maxTransX, minTransY, maxTransY, 10, 10);

        // Desencadenar computeScroll().
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        // Si se está realizando un fling, calcular el desplazamiento actual adecuado y aplicarlo.
        if (mViewState.equals(ViewState.Flinging)) {
            boolean computeVisibleTiles = false;
            if (mScroller.computeScrollOffset()) {
                mCurrentMatrix.getValues(mMatrixValues);
                mCurrentMatrix.postTranslate(mScroller.getCurrX() - mMatrixValues[Matrix.MTRANS_X],
                    mScroller.getCurrY() - mMatrixValues[Matrix.MTRANS_Y]);
            }
            else {
                setState(ViewState.None);
                // Verificar que el desplazamiento haya finalizado dentro de los límites establecidos.
                verifyAndCorrectImagePosition();
                // Calcular Tiles visibles, dado que el desplazamiento finalizó.
                computeVisibleTiles = true;
            }
            updateImageMatrix(computeVisibleTiles);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Clase que controla las acciones llevadas a cabo al detectar eventos de escala en pantalla (pinch).
     */
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
    //endregion
}