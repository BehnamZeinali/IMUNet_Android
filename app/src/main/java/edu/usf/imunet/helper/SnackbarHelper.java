package edu.usf.imunet.helper;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import edu.usf.imunet.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public final class SnackbarHelper {
    private static final int BACKGROUND_COLOR = 0xbf323232;
    private Snackbar messageSnackbar;
    private enum DismissBehavior { HIDE, SHOW, FINISH };
    private int maxLines = 2;
    private String lastMessage = "";
    private View snackbarView;

    public boolean isShowing() {
        return messageSnackbar != null;
    }

    /** Shows a snackbar with a given message. */
    public void showMessage(Activity activity, String message) {
        if (!message.isEmpty() && (!isShowing() || !lastMessage.equals(message))) {
            lastMessage = message;
            show(activity, message, DismissBehavior.HIDE);
        }
    }

    /** Shows a snackbar with a given message, and a dismiss button. */
    public void showMessageWithDismiss(Activity activity, String message) {
        show(activity, message, DismissBehavior.SHOW);
    }

    /**
     * Shows a snackbar with a given error message. When dismissed, will finish the activity. Useful
     * for notifying errors, where no further interaction with the activity is possible.
     */
    public void showError(Activity activity, String errorMessage) {
        show(activity, errorMessage, DismissBehavior.FINISH);
    }

    /**
     * Hides the currently showing snackbar, if there is one. Safe to call from any thread. Safe to
     * call even if snackbar is not shown.
     */
    public void hide(Activity activity) {
        if (!isShowing()) {
            return;
        }
        lastMessage = "";
        final Snackbar messageSnackbarToHide = messageSnackbar;
        messageSnackbar = null;
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        messageSnackbarToHide.dismiss();
                    }
                });
    }

    public void setMaxLines(int lines) {
        maxLines = lines;
    }

    /**
     * Sets the view that will be used to find a suitable parent view to hold the Snackbar view.
     *
     * <p>To use the root layout ({@link android.R.id.content}), pass in {@code null}.
     *
     * @param snackbarView the view to pass to {@link
     *     com.google.android.material.snackbar.Snackbar#make(…)} which will be used to find a
     *     suitable parent, which is a {@link androidx.coordinatorlayout.widget.CoordinatorLayout}, or
     *     the window decor's content view, whichever comes first.
     */
    public void setParentView(View snackbarView) {
        this.snackbarView = snackbarView;
    }

    private void show(
            final Activity activity, final String message, final DismissBehavior dismissBehavior) {
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        messageSnackbar =
                                Snackbar.make(
                                        snackbarView == null
                                                ? activity.findViewById(android.R.id.content)
                                                : snackbarView,
                                        message,
                                        Snackbar.LENGTH_INDEFINITE);
                        messageSnackbar.getView().setBackgroundColor(BACKGROUND_COLOR);
                        if (dismissBehavior != DismissBehavior.HIDE) {
                            messageSnackbar.setAction(
                                    "Dismiss",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            messageSnackbar.dismiss();
                                        }
                                    });
                            if (dismissBehavior == DismissBehavior.FINISH) {
                                messageSnackbar.addCallback(
                                        new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                            @Override
                                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                                super.onDismissed(transientBottomBar, event);
                                                activity.finish();
                                            }
                                        });
                            }
                        }
                        ((TextView)
                                messageSnackbar
                                        .getView()
                                        .findViewById(R.id.snackbar_text))
                                .setMaxLines(maxLines);
                        messageSnackbar.show();
                    }
                });
    }
}


