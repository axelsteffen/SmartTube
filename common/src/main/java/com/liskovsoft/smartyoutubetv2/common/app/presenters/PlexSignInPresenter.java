package com.liskovsoft.smartyoutubetv2.common.app.presenters;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.plexapi.PlexServiceManager;
import com.liskovsoft.plexserviceinterfaces.PlexSignInService;
import com.liskovsoft.plexserviceinterfaces.data.PlexAuthPin;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs.PlexServerSelectionPresenter;

import io.reactivex.disposables.Disposable;

/**
 * Fork-only: Plex PIN auth via existing {@link SignInView} (Phase 3.5).
 * After a successful claim, opens server selection.
 */
public class PlexSignInPresenter extends SignInPresenter {
    private static final String TAG = PlexSignInPresenter.class.getSimpleName();
    private static final String SIGN_IN_URL = "https://plex.tv/link";

    @SuppressLint("StaticFieldLeak")
    private static PlexSignInPresenter sInstance;
    private Disposable mSignInAction;

    private PlexSignInPresenter(Context context) {
        super(context);
    }

    public static PlexSignInPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new PlexSignInPresenter(context);
        }
        sInstance.setContext(context);
        return sInstance;
    }

    public void unhold() {
        RxHelper.disposeActions(mSignInAction);
        sInstance = null;
    }

    @Override
    public void onViewDestroyed() {
        super.onViewDestroyed();
        unhold();
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();
        RxHelper.disposeActions(mSignInAction);
        startPinFlow();
    }

    @Override
    public void onActionClicked() {
        if (getView() != null) {
            getView().close();
        }
    }

    @Override
    public void start() {
        super.start();
        RxHelper.disposeActions(mSignInAction);
    }

    private void startPinFlow() {
        PlexSignInService signInService = PlexServiceManager.instance().getSignInService();
        mSignInAction = signInService.signInWithPinObserve()
                .subscribe(
                        this::showPin,
                        error -> {
                            Log.e(TAG, "Plex sign-in error: %s", error.getMessage());
                            if (getView() != null) {
                                getView().showCode(
                                        error.getMessage() != null ? error.getMessage() : "Error",
                                        SIGN_IN_URL);
                            }
                        },
                        () -> {
                            Context ctx = getContext();
                            if (getView() != null) {
                                getView().close();
                            }
                            if (ctx != null) {
                                PlexServerSelectionPresenter.instance(ctx).show(true);
                            }
                        }
                );
    }

    private void showPin(PlexAuthPin pin) {
        if (getView() == null || pin == null || pin.getCode() == null) {
            return;
        }
        String authUrl = pin.getAuthUrl() != null ? pin.getAuthUrl() : SIGN_IN_URL;
        // fullSignInUrl drives the QR code; plex.tv/link does not embed the pin code.
        getView().showCode(pin.getCode(), authUrl, authUrl);
    }
}
