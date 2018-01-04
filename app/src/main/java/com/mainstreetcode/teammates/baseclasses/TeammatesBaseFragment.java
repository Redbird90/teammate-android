package com.mainstreetcode.teammates.baseclasses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.mainstreetcode.teammates.util.Validator;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

/**
 * Base Fragment for this app
 */

public class TeammatesBaseFragment extends BaseFragment {

    protected static final Validator validator = new Validator();

    protected CompositeDisposable disposables = new CompositeDisposable();

    protected Consumer<Throwable> defaultErrorHandler;
    protected Consumer<Throwable> emptyErrorHandler = ErrorHandler.EMPTY;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        defaultErrorHandler = ErrorHandler.builder()
                .defaultMessage(getString(R.string.default_error))
                .add(this::handleErrorMessage)
                .build();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toggleToolbar(showsToolBar());
        toggleFab(showsFab());
        toggleBottombar(showsBottomNav());
    }

    @Override
    public void onPause() {
        disposables.clear();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        getPersistentUiController().setFabClickListener(null);
        disposables.clear();
        super.onDestroyView();
    }

    public int[] staticViews() {
        return new int[]{};
    }

    public boolean drawsBehindStatusBar() {
        return false;
    }

    protected boolean showsFab() {
        return false;
    }

    protected boolean showsToolBar() {
        return true;
    }

    protected boolean showsBottomNav() {
        return true;
    }

    protected void toggleFab(boolean show) {getPersistentUiController().toggleFab(show);}

    protected void toggleToolbar(boolean show) {getPersistentUiController().toggleToolbar(show);}

    protected void toggleProgress(boolean show) {getPersistentUiController().toggleProgress(show);}

    private void toggleBottombar(boolean show) {getPersistentUiController().toggleBottombar(show);}

    protected void setFabIcon(@DrawableRes int icon) {getPersistentUiController().setFabIcon(icon);}

    protected void setToolbarTitle(CharSequence title) {getPersistentUiController().setToolbarTitle(title);}

    protected void showSnackbar(CharSequence message) {getPersistentUiController().showSnackBar(message);}

    protected void setFabClickListener(@Nullable View.OnClickListener clickListener) {getPersistentUiController().setFabClickListener(clickListener);}

    @Nullable
    @Override
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        return beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void setEnterExitTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition baseTransition = new Fade();
            Transition baseSharedTransition = new TransitionSet()
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform())
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);

            setEnterTransition(baseTransition);
            setExitTransition(baseTransition);
            setSharedElementEnterTransition(baseSharedTransition);
            setSharedElementReturnTransition(baseSharedTransition);
        }
    }

    protected void removeEnterExitTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterTransition(new Fade());
            setExitTransition(new Fade());
            setSharedElementEnterTransition(null);
            setSharedElementReturnTransition(null);
        }
    }

    protected void handleErrorMessage(Message message) {
        showSnackbar(message.getMessage());
        toggleProgress(false);
    }

    @SuppressLint("CommitTransaction")
    @SuppressWarnings("ConstantConditions")
    protected final FragmentTransaction beginTransaction() {
        return getFragmentManager().beginTransaction();
    }

    private PersistentUiController getPersistentUiController() {
        Activity activity = getActivity();
        return activity == null ? DUMMY : ((PersistentUiController) activity);
    }

    private static final PersistentUiController DUMMY = new PersistentUiController() {
        @Override
        public void toggleToolbar(boolean show) {

        }

        @Override
        public void toggleBottombar(boolean show) {

        }

        @Override
        public void toggleFab(boolean show) {

        }

        @Override
        public void toggleProgress(boolean show) {

        }

        @Override
        public void setFabIcon(int icon) {

        }

        @Override
        public void showSnackBar(CharSequence message) {

        }

        @Override
        public void setToolbarTitle(CharSequence title) {

        }

        @Override
        public void setFabClickListener(View.OnClickListener clickListener) {

        }
    };
}
