package cm.aptoide.pt.app.view;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import cm.aptoide.pt.AptoideApplication;
import cm.aptoide.pt.R;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.dataprovider.model.v7.GetAppMeta;
import cm.aptoide.pt.networking.image.ImageLoader;
import cm.aptoide.pt.utils.AptoideUtils;
import cm.aptoide.pt.view.Translator;
import cm.aptoide.pt.view.recycler.widget.Widget;
import com.jakewharton.rxbinding.view.RxView;

public class OfficialAppWidget extends Widget<OfficialAppDisplayable> {

  private static final String TAG = OfficialAppWidget.class.getName();

  private ImageView appImage;
  private Button installButton;
  private TextView installMessage;
  private TextView appName;
  private RatingBar appRating;
  private View verticalSeparator;
  private TextView appDownloads;
  private TextView appVersion;
  private TextView appSize;

  public OfficialAppWidget(View itemView) {
    super(itemView);
  }

  @Override protected void assignViews(View itemView) {
    appImage = itemView.findViewById(R.id.app_image);
    installButton = itemView.findViewById(R.id.app_install_button);
    installMessage = itemView.findViewById(R.id.install_message);
    appName = itemView.findViewById(R.id.app_name);
    verticalSeparator = itemView.findViewById(R.id.vertical_separator);
    appRating = itemView.findViewById(R.id.app_rating);
    appDownloads = itemView.findViewById(R.id.app_downloads);
    appVersion = itemView.findViewById(R.id.app_version);
    appSize = itemView.findViewById(R.id.app_size);
  }

  @Override public void bindView(OfficialAppDisplayable displayable, int position) {

    final FragmentActivity context = getContext();
    final Pair<String, GetAppMeta> appMeta = displayable.getMessageGetApp();
    final boolean isAppInstalled = displayable.isAppInstalled();

    int color = displayable.getPrimaryColor();

    final GetAppMeta.App appData = appMeta.second.getData();
    final String appName = appData.getName();

    if (!TextUtils.isEmpty(appMeta.first)) {

      // get multi part message
      final String[] parts =
          Translator.translateToMultiple(appMeta.first, getContext().getApplicationContext());
      if (parts != null && parts.length == 4) {
        SpannableString middle =
            new SpannableString(String.format(isAppInstalled ? parts[3] : parts[2], appName));
        middle.setSpan(new ForegroundColorSpan(color), 0, middle.length(), Spanned.SPAN_MARK_MARK);

        SpannableStringBuilder text = new SpannableStringBuilder();
        text.append(parts[0]);
        text.append(middle);
        text.append(parts[1]);
        installMessage.setText(text);
      } else {
        installMessage.setText(appMeta.first);
      }
    } else {
      hideOfficialAppMessage();
    }

    appRating.setRating(appData.getStats()
        .getRating()
        .getAvg());

    this.appName.setText(appName);
    this.appDownloads.setText(String.format(context.getString(R.string.downloads_count),
        AptoideUtils.StringU.withSuffix(appData.getStats()
            .getDownloads())));

    this.appVersion.setText(String.format(context.getString(R.string.version_number),
        appData.getFile()
            .getVername()));

    this.appSize.setText(String.format(context.getString(R.string.app_size),
        AptoideUtils.StringU.formatBytes(appData.getFile()
            .getFilesize(), false)));

    ImageLoader.with(context)
        .load(appData.getIcon(), this.appImage);

    // check if app is installed. if it is, show open button

    // apply button background
    installButton.setBackgroundResource(displayable.getRaisedButtonDrawable());

    installButton.setText(context.getString(isAppInstalled ? R.string.open : R.string.install));

    compositeSubscription.add(RxView.clicks(installButton)
        .subscribe(a -> {
          if (isAppInstalled) {
            AptoideUtils.SystemU.openApp(appData.getPackageName(), getContext().getPackageManager(),
                getContext());
          } else {
            // show app view to install app
            Fragment appView = AptoideApplication.getFragmentProvider()
                .newAppViewFragment(appData.getPackageName(),
                    AppViewFragment.OpenType.OPEN_AND_INSTALL);
            getFragmentNavigator().navigateTo(appView, true);
          }
        }, err -> {
          CrashReport.getInstance()
              .log(err);
        }));
  }

  private void hideOfficialAppMessage() {
    installMessage.setVisibility(View.GONE);
    verticalSeparator.setVisibility(View.GONE);
  }
}
