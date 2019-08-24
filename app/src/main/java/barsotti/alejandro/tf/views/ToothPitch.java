package barsotti.alejandro.tf.views;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import barsotti.alejandro.tf.R;
import barsotti.alejandro.tf.interfaces.IOnToothPitchChangeListener;
import barsotti.alejandro.tf.interfaces.IToothPitch;
import barsotti.alejandro.tf.utils.MathUtils;

public class ToothPitch extends Shape implements IToothPitch {
    //region Constantes
    /**
     * Tag utilizado con fines de debug.
     */
    private static final String TAG = "ToothPitch";
    /**
     * Número que indica la cantidad de puntos que componen la circunferencia.
     */
    private static final int NUMBER_OF_POINTS = 2;
    /**
     * Color utilizado para la representación gráfica de la forma.
     */
    private static final int SHAPE_COLOR = Color.LTGRAY;
    //endregion

    //region Propiedades
    /**
     * Lista de suscriptores a eventos de actualización de la circunferencia.
     */
    private ArrayList<IOnToothPitchChangeListener> objectListeners = new ArrayList<>();
    /**
     * Cultura utilizada para la representación gráfica de la medida de la escala. Permite que se muestre el
     * separador de decimales correcto para la región (coma).
     */
    private Locale locale = new Locale("es", "ES");
    private float toothPitchValue;
    private float millimetersPerPixel;
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //endregion

    //region Constructores
    public ToothPitch(Context context) {
        this(context, null);
    }

    public ToothPitch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Constructor.");
    }
    //endregion


    //region Administración de Listeners
    @Override
    public void addOnToothPitchChangeListener(IOnToothPitchChangeListener listener) {
        objectListeners.add(listener);
        listener.onToothPitchValueChange(millimetersPerPixel);
    }

    @Override
    public void removeOnToothPitchChangeListener(IOnToothPitchChangeListener listener) {
        try {
            objectListeners.remove(listener);
        }
        catch (Exception e) {
            Log.d(TAG, "removeOnToothPitchChangeListener: the object was not found in the list.");
        }
    }
    //endregion

    @Override
    protected void initializeShape() {
        // Establecer parámetros de la pintura a utilizar para la representación gráfica del paso.
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(mShapePaint.getColor());
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setShadowLayer(5, 0, 0, Color.BLACK);
        textPaint.setTextSize(48);
    }

    @Override
    public void selectShape(boolean isSelected) {
        super.selectShape(isSelected);

        if (mShapePoints.size() == NUMBER_OF_POINTS && isSelected) {
            showToothPitchInputDialog();
        }
    }

    private void showToothPitchInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.tooth_pitch_input_dialog_title);

        // Configurar el tipo de campo.
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        // Configurar botones.
        builder.setPositiveButton(R.string.tooth_pitch_input_dialog_positive_button_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String inputText = input.getText().toString();
                    inputText = inputText.replace(',', '.');
                    toothPitchValue = Float.parseFloat(inputText);
                    computeShape();

                    // Informar a los suscriptores sobre la actualización del valor de la escala.
                    for (IOnToothPitchChangeListener listener: objectListeners) {
                        listener.onToothPitchValueChange(millimetersPerPixel);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Toast
                        .makeText(getContext(),getContext().getString(R.string.not_a_number_error_message),
                            Toast.LENGTH_SHORT)
                        .show();
                }
            }
        });
        builder.setNegativeButton(R.string.tooth_pitch_input_dialog_negative_button_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    protected int getNumberOfPointsInShape() {
        return NUMBER_OF_POINTS;
    }

    @Override
    protected int getShapeColor() {
        return SHAPE_COLOR;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
            // Dibujar borde de la forma.
            canvas.drawLine(mMappedShapePoints.get(0).x, mMappedShapePoints.get(0).y,
                mMappedShapePoints.get(1).x, mMappedShapePoints.get(1).y,
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
            // Dibujar línea principal de la forma.
            canvas.drawLine(mMappedShapePoints.get(0).x, mMappedShapePoints.get(0).y,
                mMappedShapePoints.get(1).x, mMappedShapePoints.get(1).y,
                mIsSelected ? mSelectedShapePaint : mShapePaint);
            if (mIsSelected) {
                canvas.drawText(
                    toothPitchValue != 0 ? String.format(locale, "%.2f mm", toothPitchValue) : "?",
                    mMappedShapePoints.get(1).x + mPointRadius,
                    mMappedShapePoints.get(1).y + mPointRadius, textPaint);
            }
        }

        super.onDraw(canvas);
    }

    @Override
    public float computeDistanceBetweenTouchAndShape(PointF point) {
        float distanceBetweenTouchAndShape = MathUtils.distanceBetweenSegmentAndPoint(point,
            mMappedShapePoints.get(0), mMappedShapePoints.get(1));

        return distanceBetweenTouchAndShape <= TOUCH_TOLERANCE ? distanceBetweenTouchAndShape : -1;
    }

    @Override
    protected void computeShape() {
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
            if (toothPitchValue == 0) {
                showToothPitchInputDialog();
            }

            float distanceBetweenPoints = MathUtils.distanceBetweenPoints(mShapePoints.get(0).x,
                mShapePoints.get(0).y, mShapePoints.get(1).x, mShapePoints.get(1).y);
            millimetersPerPixel = toothPitchValue / distanceBetweenPoints;
        }
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        super.updateViewMatrix(matrix);

        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);

        invalidate();
    }
}
