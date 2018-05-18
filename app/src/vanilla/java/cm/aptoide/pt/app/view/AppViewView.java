package cm.aptoide.pt.app.view;

import android.view.MenuItem;
import cm.aptoide.pt.app.DetailedAppViewModel;
import cm.aptoide.pt.app.ReviewsViewModel;
import cm.aptoide.pt.app.SimilarAppsViewModel;
import cm.aptoide.pt.app.view.screenshots.ScreenShotClickEvent;
import cm.aptoide.pt.presenter.View;
import cm.aptoide.pt.share.ShareDialogs;
import cm.aptoide.pt.utils.GenericDialogs;
import cm.aptoide.pt.view.app.DetailedAppRequestResult;
import cm.aptoide.pt.view.app.FlagsVote;
import rx.Observable;

/**
 * Created by franciscocalado on 08/05/18.
 */

public interface AppViewView extends View {

  void showLoading();

  void showAppview();

  long getAppId();

  String getPackageName();

  void populateAppDetails(DetailedAppViewModel detailedApp);

  void handleError(DetailedAppRequestResult.Error error);

  Observable<ScreenShotClickEvent> getScreenshotClickEvent();

  Observable<ReadMoreClickEvent> clickedReadMore();

  void populateReviews(ReviewsViewModel reviews, DetailedAppViewModel app);

  void populateAds(SimilarAppsViewModel ads);

  Observable<FlagsVote.VoteType> clickWorkingFlag();

  Observable<FlagsVote.VoteType> clickLicenseFlag();

  Observable<FlagsVote.VoteType> clickFakeFlag();

  Observable<FlagsVote.VoteType> clickVirusFlag();

  void displayNotLoggedInSnack();

  void displayStoreFollowedSnack(String storeName);

  Observable<Void> clickDeveloperWebsite();

  Observable<Void> clickDeveloperEmail();

  Observable<Void> clickDeveloperPrivacy();

  Observable<Void> clickDeveloperPermissions();

  Observable<Void> clickStoreLayout();

  Observable<Void> clickFollowStore();

  Observable<Void> clickOtherVersions();

  Observable<Void> clickTrustedBadge();

  Observable<Void> clickRateApp();

  Observable<Void> clickRateAppLarge();

  Observable<Void> clickRateAppLayout();

  Observable<Void> clickCommentsLayout();

  Observable<Void> clickReadAllComments();

  Observable<Void> clickLoginSnack();

  Observable<SimilarAppClickEvent> clickSimilarApp();

  Observable<MenuItem> clickToolbar();

  Observable<Void> clickNoNetworkRetry();

  Observable<Void> clickGenericRetry();

  Observable<ShareDialogs.ShareResponse> shareDialogResponse();

  Observable<Integer> scrollReviewsResponse();

  void navigateToDeveloperWebsite(DetailedAppViewModel app);

  void navigateToDeveloperEmail(DetailedAppViewModel app);

  void navigateToDeveloperPrivacy(DetailedAppViewModel app);

  void navigateToDeveloperPermissions(DetailedAppViewModel app);

  void setFollowButton(boolean isFollowing);

  void showTrustedDialog(DetailedAppViewModel app);

  String getLanguageFilter();

  Observable<GenericDialogs.EResponse> showRateDialog(String appName, String packageName,
      String storeName);

  void disableFlags();

  void enableFlags();

  void incrementFlags(FlagsVote.VoteType type);

  void showFlagVoteSubmittedMessage();

  void showShareDialog();

  void showShareOnTvDialog();

  void defaultShare(String appName, String wUrl);

  void recommendsShare(String packageName, Long storeId);

  void scrollReviews(Integer position);
}
