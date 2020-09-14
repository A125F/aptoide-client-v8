package cm.aptoide.pt.editorial;

import cm.aptoide.analytics.AnalyticsManager;
import cm.aptoide.pt.ads.MoPubAdsManager;
import cm.aptoide.pt.ads.WalletAdsOfferManager;
import cm.aptoide.pt.app.DownloadStateParser;
import cm.aptoide.pt.database.room.RoomDownload;
import cm.aptoide.pt.download.DownloadAnalytics;
import cm.aptoide.pt.download.DownloadFactory;
import cm.aptoide.pt.install.InstallAnalytics;
import cm.aptoide.pt.install.InstallManager;
import cm.aptoide.pt.notification.NotificationAnalytics;
import cm.aptoide.pt.reactions.ReactionsManager;
import cm.aptoide.pt.reactions.network.LoadReactionModel;
import cm.aptoide.pt.reactions.network.ReactionsResponse;
import cm.aptoide.pt.view.EditorialConfiguration;
import rx.Completable;
import rx.Observable;
import rx.Single;

/**
 * Created by D01 on 27/08/2018.
 */

public class EditorialManager {

  private final EditorialRepository editorialRepository;
  private final EditorialConfiguration editorialConfiguration;
  private final InstallManager installManager;
  private final DownloadFactory downloadFactory;
  private final NotificationAnalytics notificationAnalytics;
  private final InstallAnalytics installAnalytics;
  private final EditorialAnalytics editorialAnalytics;
  private final ReactionsManager reactionsManager;
  private final MoPubAdsManager moPubAdsManager;
  private DownloadStateParser downloadStateParser;

  public EditorialManager(EditorialRepository editorialRepository,
      EditorialConfiguration editorialConfiguration, InstallManager installManager,
      DownloadFactory downloadFactory, DownloadStateParser downloadStateParser,
      NotificationAnalytics notificationAnalytics, InstallAnalytics installAnalytics,
      EditorialAnalytics editorialAnalytics, ReactionsManager reactionsManager,
      MoPubAdsManager moPubAdsManager) {

    this.editorialRepository = editorialRepository;
    this.editorialConfiguration = editorialConfiguration;
    this.installManager = installManager;
    this.downloadFactory = downloadFactory;
    this.downloadStateParser = downloadStateParser;
    this.notificationAnalytics = notificationAnalytics;
    this.installAnalytics = installAnalytics;
    this.editorialAnalytics = editorialAnalytics;
    this.reactionsManager = reactionsManager;
    this.moPubAdsManager = moPubAdsManager;
  }

  public Single<EditorialViewModel> loadEditorialViewModel() {
    return editorialRepository.loadEditorialViewModel(editorialConfiguration.getLoadSource());
  }

  public boolean shouldShowRootInstallWarningPopup() {
    return installManager.showWarning();
  }

  public void allowRootInstall(Boolean answer) {
    installManager.rootInstallAllowed(answer);
  }

  public Completable downloadApp(EditorialDownloadEvent editorialDownloadEvent) {
    return Observable.just(downloadFactory.create(
        downloadStateParser.parseDownloadAction(editorialDownloadEvent.getAction()),
        editorialDownloadEvent.getAppName(), editorialDownloadEvent.getPackageName(),
        editorialDownloadEvent.getMd5(), editorialDownloadEvent.getIcon(),
        editorialDownloadEvent.getVerName(), editorialDownloadEvent.getVerCode(),
        editorialDownloadEvent.getPath(), editorialDownloadEvent.getPathAlt(),
        editorialDownloadEvent.getObb(), false, editorialDownloadEvent.getSize(),
        editorialDownloadEvent.getSplits(), editorialDownloadEvent.getRequiredSplits(),
        editorialDownloadEvent.getTrustedBadge(), editorialDownloadEvent.getTrustedBadge()))
        .flatMapSingle(download -> moPubAdsManager.getAdsVisibilityStatus()
            .doOnSuccess(offerResponseStatus -> setupDownloadEvents(download,
                editorialDownloadEvent.getPackageName(), editorialDownloadEvent.getAppId(),
                offerResponseStatus, editorialDownloadEvent.getTrustedBadge(),
                editorialDownloadEvent.getStoreName(), editorialDownloadEvent.getAction()
                    .toString()))
            .map(__ -> download))
        .flatMapCompletable(download -> installManager.install(download))
        .toCompletable();
  }

  private void setupDownloadEvents(RoomDownload download, String packageName, long appId,
      WalletAdsOfferManager.OfferResponseStatus offerResponseStatus, String trustedBadge,
      String storeName, String installType) {
    int campaignId = notificationAnalytics.getCampaignId(packageName, appId);
    String abTestGroup = notificationAnalytics.getAbTestingGroup(packageName, appId);
    editorialAnalytics.setupDownloadEvents(download, campaignId, abTestGroup,
        AnalyticsManager.Action.CLICK, offerResponseStatus, trustedBadge, storeName, installType);
    installAnalytics.installStarted(download.getPackageName(), download.getVersionCode(),
        AnalyticsManager.Action.INSTALL, DownloadAnalytics.AppContext.EDITORIAL,
        downloadStateParser.getOrigin(download.getAction()), campaignId, abTestGroup, false,
        download.hasAppc(), download.hasSplits(), offerResponseStatus.toString(),
        download.getTrustedBadge(), download.getStoreName(), false);
  }

  public Observable<EditorialDownloadModel> loadDownloadModel(String md5, String packageName,
      int versionCode, int position) {
    return installManager.getInstall(md5, packageName, versionCode)
        .map(install -> new EditorialDownloadModel(
            downloadStateParser.parseDownloadType(install.getType(), false), install.getProgress(),
            downloadStateParser.parseDownloadState(install.getState(), install.isIndeterminate()),
            position));
  }

  public Completable pauseDownload(String md5) {
    return installManager.pauseInstall(md5);
  }

  public Completable resumeDownload(String md5, String packageName, long appId, String action) {
    return installManager.getDownload(md5)
        .flatMap(download -> moPubAdsManager.getAdsVisibilityStatus()
            .doOnSuccess(offerResponseStatus -> setupDownloadEvents(download, packageName, appId,
                offerResponseStatus, download.getTrustedBadge(), download.getStoreName(), action))
            .map(__ -> download))
        .flatMapCompletable(download -> installManager.install(download));
  }

  public Completable cancelDownload(String md5, String packageName, int versionCode) {
    return installManager.cancelInstall(md5, packageName, versionCode);
  }

  public Single<LoadReactionModel> loadReactionModel(String cardId, String groupId) {
    return reactionsManager.loadReactionModel(cardId, groupId);
  }

  public Single<ReactionsResponse> setReaction(String cardId, String groupId, String reaction) {
    return reactionsManager.setReaction(cardId, groupId, reaction);
  }

  public Single<ReactionsResponse> deleteReaction(String cardId, String groupId) {
    return reactionsManager.deleteReaction(cardId, groupId);
  }

  public Single<Boolean> isFirstReaction(String cardId, String groupId) {
    return reactionsManager.isFirstReaction(cardId, groupId);
  }
}
