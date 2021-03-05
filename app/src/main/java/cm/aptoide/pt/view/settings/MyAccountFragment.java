package cm.aptoide.pt.view.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import cm.aptoide.accountmanager.Account;
import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.analytics.implementation.navigation.ScreenTagHistory;
import cm.aptoide.aptoideviews.socialmedia.SocialMediaView;
import cm.aptoide.pt.AptoideApplication;
import cm.aptoide.pt.MyAccountManager;
import cm.aptoide.pt.R;
import cm.aptoide.pt.account.AccountAnalytics;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.dataprovider.WebService;
import cm.aptoide.pt.dataprovider.model.v7.store.GetStore;
import cm.aptoide.pt.dataprovider.model.v7.store.Store;
import cm.aptoide.pt.dataprovider.ws.BodyInterceptor;
import cm.aptoide.pt.dataprovider.ws.v7.BaseBody;
import cm.aptoide.pt.dataprovider.ws.v7.BaseRequestWithStore;
import cm.aptoide.pt.dataprovider.ws.v7.store.GetStoreRequest;
import cm.aptoide.pt.dataprovider.ws.v7.store.StoreContext;
import cm.aptoide.pt.link.CustomTabsHelper;
import cm.aptoide.pt.networking.image.ImageLoader;
import cm.aptoide.pt.socialmedia.SocialMediaAnalytics;
import cm.aptoide.pt.themes.ThemeManager;
import cm.aptoide.pt.view.BackButtonFragment;
import cm.aptoide.pt.view.NotBottomNavigationView;
import com.jakewharton.rxbinding.view.RxView;
import javax.inject.Inject;
import javax.inject.Named;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by franciscocalado on 12/03/18.
 */

public class MyAccountFragment extends BackButtonFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener, MyAccountView,
    NotBottomNavigationView {

  private static final float STROKE_SIZE = 0.04f;
  protected Toolbar toolbar;
  @Inject MyAccountNavigator myAccountNavigator;
  @Inject AccountAnalytics accountAnalytics;
  @Inject MyAccountManager myAccountManager;
  @Inject @Named("marketName") String marketName;
  @Inject ThemeManager themeManager;
  @Inject SocialMediaAnalytics socialMediaAnalytics;
  private AptoideAccountManager accountManager;
  private Converter.Factory converterFactory;
  private OkHttpClient httpClient;
  private BodyInterceptor<BaseBody> bodyInterceptor;
  //Account views
  private View myProfileView;
  private View myStoreView;
  private View loginView;
  private View accountView;
  private TextView createStoreMessage;
  private ImageView myAccountAvatar;
  private ImageView myStoreAvatar;
  private TextView myAccountName;
  private TextView myStoreName;
  private Button loginButton;
  private Button logoutButton;
  private Button createStoreButton;
  private Button editStoreButton;
  private Button editProfileButton;
  private CardView aptoideTvCardView;
  private CardView aptoideUploaderCardView;
  private CardView aptoideBackupAppsCardView;
  private View settings;
  private TextView myAccountProductCardTitle;
  private SocialMediaView socialMediaView;

  public static Fragment newInstance() {
    return new MyAccountFragment();
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getFragmentComponent(savedInstanceState).inject(this);

    final AptoideApplication application =
        (AptoideApplication) getContext().getApplicationContext();
    accountManager =
        ((AptoideApplication) getContext().getApplicationContext()).getAccountManager();
    bodyInterceptor = application.getAccountSettingsBodyInterceptorPoolV7();
    httpClient = application.getDefaultClient();
    converterFactory = WebService.getDefaultConverter();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ((TextView) view.findViewById(R.id.sign_in_message)).setText(
        getString(R.string.newaccount_signin_message, marketName));
    toolbar = view.findViewById(R.id.toolbar);
    settings = view.findViewById(R.id.settings);
    myAccountProductCardTitle = view.findViewById(R.id.my_account_product_card_title);
    myAccountProductCardTitle.setText(getString(R.string.my_account_product_card_section_title));

    setAccountViews(view);
    setupToolbar();
    setupProductCardViews();

    attachPresenter(new MyAccountPresenter(this, accountManager, CrashReport.getInstance(),
        AndroidSchedulers.mainThread(), myAccountNavigator, accountAnalytics,
        socialMediaAnalytics));
  }

  @Override public ScreenTagHistory getHistoryTracker() {
    return ScreenTagHistory.Builder.build(this.getClass()
        .getSimpleName());
  }

  private void setupProductCardViews() {
    //Aptoide TV
    ((TextView) aptoideTvCardView.findViewById(R.id.product_title_textview)).setText(
        getString(R.string.product_card_aptoide_tv_title));
    ((TextView) aptoideTvCardView.findViewById(R.id.product_subtitle_textview)).setText(
        getString(R.string.product_card_aptoide_tv_subtitle));
    ((ImageView) aptoideTvCardView.findViewById(R.id.product_icon_imageview)).setImageDrawable(
        ContextCompat.getDrawable(getContext(), R.drawable.ic_product_tv));

    //Aptoide Uploader
    ((TextView) aptoideUploaderCardView.findViewById(R.id.product_title_textview)).setText(
        getString(R.string.product_card_aptoide_uploader_title));
    ((TextView) aptoideUploaderCardView.findViewById(R.id.product_subtitle_textview)).setText(
        getString(R.string.product_card_aptoide_uploader_subtitle));
    ((ImageView) aptoideUploaderCardView.findViewById(
        R.id.product_icon_imageview)).setImageDrawable(
        ContextCompat.getDrawable(getContext(), R.drawable.ic_product_uploader));

    //Aptoide Backup
    ((TextView) aptoideBackupAppsCardView.findViewById(R.id.product_title_textview)).setText(
        getString(R.string.product_card_aptoide_backup_apps_title));
    ((TextView) aptoideBackupAppsCardView.findViewById(R.id.product_subtitle_textview)).setText(
        getString(R.string.product_card_aptoide_backup_apps_subtitle));
    ((ImageView) aptoideBackupAppsCardView.findViewById(
        R.id.product_icon_imageview)).setImageDrawable(
        ContextCompat.getDrawable(getContext(), R.drawable.ic_product_backup_apps));
  }

  @Override public void onDestroyView() {
    myProfileView = null;
    myStoreView = null;
    loginView = null;
    accountView = null;
    createStoreMessage = null;
    myAccountAvatar = null;
    myAccountName = null;
    myStoreName = null;
    loginButton = null;
    logoutButton = null;
    createStoreButton = null;
    editStoreButton = null;
    editProfileButton = null;
    aptoideBackupAppsCardView = null;
    aptoideTvCardView = null;
    aptoideUploaderCardView = null;
    socialMediaView = null;
    super.onDestroyView();
  }

  @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_my_account, container, false);
  }

  @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

  }

  @Override public void showAccount(Account account) {
    if (TextUtils.isEmpty(account.getEmail())) {
      showLoginAccountDisplayable();
    } else if (account.getStore()
        .getName()
        .isEmpty()) {
      showAccountNoStoreDisplayable();
      setUserProfile(account);
    } else {
      showAccountAndStoreDisplayable();
      setUserProfile(account);
      setUserStore(account.getStore()
          .getName(), account.getStore()
          .getAvatar());
    }
  }

  @Override public Observable<Void> loginClick() {
    return RxView.clicks(loginButton);
  }

  @Override public Observable<Void> signOutClick() {
    return RxView.clicks(logoutButton);
  }

  @Override public Observable<Void> storeClick() {
    return RxView.clicks(myStoreView);
  }

  @Override public Observable<Void> userClick() {
    return RxView.clicks(myProfileView);
  }

  @Override public Observable<Void> editStoreClick() {
    return RxView.clicks(editStoreButton);
  }

  @Override public Observable<Void> editUserProfileClick() {
    return RxView.clicks(editProfileButton);
  }

  @Override public Observable<Void> settingsClicked() {
    return RxView.clicks(settings);
  }

  @Override public Observable<GetStore> getStore() {
    return accountManager.accountStatus()
        .first()
        .flatMap(account -> GetStoreRequest.of(new BaseRequestWithStore.StoreCredentials(
                account.getStore()
                    .getName(), null, null), StoreContext.meta, bodyInterceptor, httpClient,
            converterFactory,
            ((AptoideApplication) getContext().getApplicationContext()).getTokenInvalidator(),
            ((AptoideApplication) getContext().getApplicationContext()).getDefaultSharedPreferences(),
            getContext().getResources(),
            (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
            .observe());
  }

  @Override public Observable<Void> aptoideTvCardViewClick() {
    return RxView.clicks(aptoideTvCardView);
  }

  @Override public Observable<Void> aptoideUploaderCardViewClick() {
    return RxView.clicks(aptoideUploaderCardView);
  }

  @Override public Observable<Void> aptoideBackupCardViewClick() {
    return RxView.clicks(aptoideBackupAppsCardView);
  }

  @Override public void startAptoideTvWebView() {
    CustomTabsHelper.getInstance()
        .openInChromeCustomTab("https://blog.aptoide.com/what-is-aptoidetv/", getContext(),
            themeManager.getAttributeForTheme(R.attr.colorPrimary).resourceId);
  }

  @Override public void refreshUI(Store store) {
    myStoreName.setText(store.getName());
    setUserStore(store.getName(), store.getAvatar());
  }

  @Override public void showLoginAccountDisplayable() {
    loginView.setVisibility(View.VISIBLE);
    accountView.setVisibility(View.GONE);
  }

  @Override public Observable<Void> createStoreClick() {
    return RxView.clicks(createStoreButton);
  }

  @Override public Observable<SocialMediaView.SocialMediaType> socialMediaClick() {
    return socialMediaView.onSocialMediaClick();
  }

  private void showAccountNoStoreDisplayable() {
    loginView.setVisibility(View.GONE);
    accountView.setVisibility(View.VISIBLE);

    myProfileView.setVisibility(View.VISIBLE);
    myStoreView.setVisibility(View.GONE);
    if (myAccountManager.shouldShowCreateStore()) {
      createStoreButton.setVisibility(View.VISIBLE);
      createStoreMessage.setVisibility(View.VISIBLE);
    } else {
      createStoreButton.setVisibility(View.GONE);
      createStoreMessage.setVisibility(View.GONE);
    }
  }

  private void showAccountAndStoreDisplayable() {
    loginView.setVisibility(View.GONE);
    accountView.setVisibility(View.VISIBLE);

    myProfileView.setVisibility(View.VISIBLE);
    myStoreView.setVisibility(View.VISIBLE);
    createStoreButton.setVisibility(View.GONE);
    createStoreMessage.setVisibility(View.GONE);
  }

  private void setUserProfile(Account account) {
    if (!TextUtils.isEmpty(account.getNickname())) {
      myAccountName.setText(account.getNickname());
    } else {
      myAccountName.setText(account.getEmail());
    }
    if (!TextUtils.isEmpty(account.getAvatar())) {
      String userAvatarUrl = account.getAvatar();
      ImageLoader.with(getContext())
          .loadWithShadowCircleTransformWithPlaceholder(userAvatarUrl, myAccountAvatar, STROKE_SIZE,
              R.attr.placeholder_myaccount);
    }
  }

  private void setUserStore(String storeName, String storeAvatar) {
    if (!TextUtils.isEmpty(storeName)) {
      myStoreName.setText(storeName);
      ImageLoader.with(getContext())
          .loadWithShadowCircleTransformWithPlaceholder(storeAvatar, this.myStoreAvatar,
              STROKE_SIZE, R.attr.placeholder_myaccount);
    }
  }

  private void setAccountViews(View view) {
    myProfileView = view.findViewById(R.id.my_profile);
    myStoreView = view.findViewById(R.id.my_store);
    accountView = view.findViewById(R.id.account_displayables);
    loginView = view.findViewById(R.id.login_register_container);

    myAccountAvatar = myProfileView.findViewById(R.id.user_icon);
    myAccountName = myProfileView.findViewById(R.id.description);
    myStoreAvatar = myStoreView.findViewById(R.id.user_icon);
    myStoreName = myStoreView.findViewById(R.id.description);

    TextView myStoreTitle = myStoreView.findViewById(R.id.name);
    myStoreTitle.setText(R.string.newaccount_my_store);

    TextView myAccountTitle = myProfileView.findViewById(R.id.name);
    myAccountTitle.setText(R.string.newaccount_my_profile);

    loginButton = view.findViewById(R.id.login_button);
    logoutButton = view.findViewById(R.id.logout_button);
    createStoreMessage = view.findViewById(R.id.create_store_message);
    createStoreButton = view.findViewById(R.id.create_store_button);
    editStoreButton = myStoreView.findViewById(R.id.edit_button);
    editProfileButton = myProfileView.findViewById(R.id.edit_button);

    aptoideTvCardView = view.findViewById(R.id.product_aptoideTv_cardview);
    aptoideUploaderCardView = view.findViewById(R.id.product_uploader_cardview);
    aptoideBackupAppsCardView = view.findViewById(R.id.product_backup_cardview);

    socialMediaView = view.findViewById(R.id.social_media_view);
  }

  private void setupToolbar() {
    toolbar.setTitle(R.string.my_account_title_my_account);

    final AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);
    ActionBar actionBar = activity.getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle(toolbar.getTitle());
    }
    toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
  }
}