package barsotti.alejandro.tf.presenters;

import android.os.Bundle;

public class MeasurementDetailsPresenter {
    private static MeasurementDetailsPresenter instance;

    private Bundle viewState;

    private MeasurementDetailsPresenter() {
    }

    public static MeasurementDetailsPresenter getInstance() {
        if (instance == null) {
            instance = new MeasurementDetailsPresenter();
        }

        return instance;
    }

    public void clearState() {
        viewState = null;
    }

    public void saveState(Bundle bundle) {
        viewState = bundle;
    }

    public Bundle getState() {
        return viewState;
    }
}
