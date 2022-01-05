package cm.aptoide.pt.promotions;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.analytics.implementation.CrashLogger;
import cm.aptoide.pt.R;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.home.HomePromotionsWrapper;
import cm.aptoide.pt.networking.image.ImageLoader;
import cm.aptoide.pt.themes.ThemeManager;
import rx.Observable;
import rx.subjects.PublishSubject;

public class PromotionsHomeDialog {
  private static final String HOME_PROMOTIONS_DIALOG_EVENT_LISTENER_IS_NULL =
      "HOME_PROMOTIONS_DIALOG_EVENT_LISTENER_IS_NULL";
  private final CrashLogger crashReport;
  private AlertDialog dialog;
  private final View dialogView;
  private Button navigate;
  private Button cancel;
  private PublishSubject<String> uiEvents;

  public PromotionsHomeDialog(Context context) {
    this.crashReport = CrashReport.getInstance();
    uiEvents = PublishSubject.create();
    LayoutInflater inflater = LayoutInflater.from(context);
    dialog = new AlertDialog.Builder(context).create();
    dialogView = inflater.inflate(R.layout.promotions_home_dialog, null);
    dialog.setView(dialogView);
    cancel = dialogView.findViewById(R.id.cancel_button);
    navigate = dialogView.findViewById(R.id.navigate_button);
    dialog.setCancelable(true);
    dialog.setCanceledOnTouchOutside(true);

    Window window = dialog.getWindow();
    if (window != null) {
      window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    navigate.setOnClickListener(__ -> {
      if (uiEvents != null) {
        uiEvents.onNext("navigate");
      } else {
        crashReport.log(HOME_PROMOTIONS_DIALOG_EVENT_LISTENER_IS_NULL, "");
      }
    });

    cancel.setOnClickListener(__ -> {
      if (uiEvents != null) {
        uiEvents.onNext("cancel");
      } else {
        crashReport.log(HOME_PROMOTIONS_DIALOG_EVENT_LISTENER_IS_NULL, "");
      }
    });
  }

  public void showDialog(HomePromotionsWrapper promotions) {
    dialog.show();
    TextView description = dialogView.findViewById(R.id.description);
    description.setText(promotions.getDescription());
    TextView titleView = dialog.findViewById(R.id.promotion_title);
    ImageView promotionGraphicView = dialog.findViewById(R.id.promotion_graphic);
    titleView.setText(promotions.getTitle());
    ImageLoader.with(dialog.getContext())
        .load(promotions.getFeatureGraphic(), promotionGraphicView);
  }

  public void dismissDialog() {
    dialog.dismiss();
  }

  public void destroyDialog() {
    dismissDialog();
    dialog = null;
    navigate = null;
    cancel = null;
    uiEvents = null;
  }

  public Observable<String> dialogClicked() {
    return uiEvents;
  }
}