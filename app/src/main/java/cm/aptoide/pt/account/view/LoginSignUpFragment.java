package cm.aptoide.pt.account.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import cm.aptoide.analytics.implementation.navigation.ScreenTagHistory;
import cm.aptoide.pt.R;
import cm.aptoide.pt.presenter.LoginSignUpView;
import cm.aptoide.pt.view.NotBottomNavigationView;
import cm.aptoide.pt.view.fragment.BaseToolbarFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class LoginSignUpFragment extends BaseToolbarFragment
    implements LoginSignUpView, NotBottomNavigationView {

  private static final String BOTTOM_SHEET_WITH_BOTTOM_BAR = "bottom_sheet_expanded";
  private static final String DISMISS_TO_NAVIGATE_TO_MAIN_VIEW = "dismiss_to_navigate_to_main_view";
  private static final String NAVIGATE_TO_HOME = "clean_back_stack";
  private static final String IS_WIZARD = "is_wizard";
  private static final String ACCOUNT_TYPE = "account_type";
  private static final String AUTH_TYPE = "auth_type";
  private static final String IS_NEW_ACCOUNT = "is_new_account";
  public static final String HAS_MAGIC_LINK_ERROR = "has_magic_link_error";
  public static final String MAGIC_LINK_ERROR_MESSAGE = "magic_link_error_message";

  private BottomSheetBehavior<View> bottomSheetBehavior;
  private boolean withBottomBar;
  private LoginBottomSheet loginBottomSheet;
  private View mainContent;
  private int originalBottomPadding;
  private LoginSignUpPresenter presenter;
  private String toolbarTitle;
  private boolean dismissToNavigateToMainView;
  private boolean navigateToHome;
  private boolean isWizard;

  private boolean hasMagicLinkError;
  private String magicLinkErrorMessage;

  public static LoginSignUpFragment newInstance(boolean withBottomBar,
      boolean dismissToNavigateToMainView, boolean navigateToHome, boolean isWizard) {
    return newInstance(withBottomBar, dismissToNavigateToMainView, navigateToHome, "", "", true,
        isWizard, false, "");
  }

  public static LoginSignUpFragment newInstance(boolean withBottomBar,
      boolean dismissToNavigateToMainView, boolean navigateToHome, boolean isWizard,
      boolean hasMagicLinkError, String magicLinkErrorMessage) {
    return newInstance(withBottomBar, dismissToNavigateToMainView, navigateToHome, "", "", true,
        isWizard, hasMagicLinkError, magicLinkErrorMessage);
  }

  public static LoginSignUpFragment newInstance(boolean withBottomBar,
      boolean dismissToNavigateToMainView, boolean navigateToHome, String accountType,
      String authType, boolean isNewAccount, boolean isWizard, boolean hasMagicLinkError,
      String magicLinkErrorMessage) {
    Bundle args = new Bundle();
    args.putBoolean(BOTTOM_SHEET_WITH_BOTTOM_BAR, withBottomBar);
    args.putBoolean(DISMISS_TO_NAVIGATE_TO_MAIN_VIEW, dismissToNavigateToMainView);
    args.putBoolean(NAVIGATE_TO_HOME, navigateToHome);
    args.putString(ACCOUNT_TYPE, accountType);
    args.putString(AUTH_TYPE, authType);
    args.putBoolean(IS_NEW_ACCOUNT, isNewAccount);
    args.putBoolean(IS_WIZARD, isWizard);
    args.putBoolean(HAS_MAGIC_LINK_ERROR, hasMagicLinkError);
    args.putString(MAGIC_LINK_ERROR_MESSAGE, magicLinkErrorMessage);
    LoginSignUpFragment fragment = new LoginSignUpFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override public void loadExtras(Bundle args) {
    super.loadExtras(args);
    withBottomBar = args.getBoolean(BOTTOM_SHEET_WITH_BOTTOM_BAR);
    dismissToNavigateToMainView = args.getBoolean(DISMISS_TO_NAVIGATE_TO_MAIN_VIEW);
    navigateToHome = args.getBoolean(NAVIGATE_TO_HOME);
    isWizard = args.getBoolean(IS_WIZARD);

    hasMagicLinkError = args.getBoolean(LoginSignUpCredentialsFragment.HAS_MAGIC_LINK_ERROR);
    magicLinkErrorMessage = args.getString(LoginSignUpCredentialsFragment.MAGIC_LINK_ERROR_MESSAGE);
    if (magicLinkErrorMessage == null) {
      magicLinkErrorMessage = "";
    }
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof LoginBottomSheet) {
      loginBottomSheet = (LoginBottomSheet) context;
    } else {
      throw new IllegalStateException(
          "Context should implement " + LoginBottomSheet.class.getSimpleName());
    }
  }

  @Override public void onDestroyView() {
    if (bottomSheetBehavior != null) {
      bottomSheetBehavior.setBottomSheetCallback(null);
      bottomSheetBehavior = null;
    }
    if (presenter != null) {
      unregisterClickHandler(presenter);
    }
    super.onDestroyView();
  }

  @Override public void setupViews() {
    super.setupViews();
    presenter = new LoginSignUpPresenter(this, getFragmentChildNavigator(R.id.login_signup_layout),
        dismissToNavigateToMainView, navigateToHome, hasMagicLinkError, magicLinkErrorMessage);
    attachPresenter(presenter);
    registerClickHandler(presenter);
    bottomSheetBehavior.setBottomSheetCallback(presenter);
  }

  @Override protected boolean hasToolbar() {
    return toolbarTitle != null;
  }

  @Override protected boolean displayHomeUpAsEnabled() {
    return hasToolbar();
  }

  public void setupToolbarDetails(Toolbar toolbar) {
    toolbar.setTitle("");
  }

  @Override public void bindViews(View view) {
    super.bindViews(view);

    try {
      bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.login_signup_layout));
    } catch (IllegalArgumentException ex) {
      // this happens because in landscape the R.id.login_signup_layout is not
      // a child of CoordinatorLayout
    }

    if (bottomSheetBehavior != null) {
      mainContent = view.findViewById(R.id.main_content);
      originalBottomPadding = withBottomBar ? mainContent.getPaddingBottom() : 0;
      toolbarTitle = getString(R.string.my_account_title_my_account);
      mainContent.setPadding(0, 0, 0, originalBottomPadding);
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
  }

  @Override public void collapseBottomSheet() {
    loginBottomSheet.collapse();
    mainContent.setPadding(0, 0, 0, originalBottomPadding);
  }

  @Override public void expandBottomSheet() {
    loginBottomSheet.expand();
    mainContent.setPadding(0, 0, 0, 0);
  }

  @Override public boolean bottomSheetIsExpanded() {
    return bottomSheetBehavior != null
        && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
  }

  @Override public void setBottomSheetState(int stateCollapsed) {
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
  }

  @Override public int getContentViewId() {
    if (isWizard) {
      return R.layout.fragment_login_signup_wizard_layout;
    } else {
      return R.layout.fragment_login_sign_up;
    }
  }

  @Override public ScreenTagHistory getHistoryTracker() {
    return ScreenTagHistory.Builder.build(this.getClass()
        .getSimpleName());
  }
}
