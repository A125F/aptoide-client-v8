package cm.aptoide.pt.v8engine.install;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageInfo;
import cm.aptoide.pt.crashreports.CrashReports;
import cm.aptoide.pt.database.accessors.AccessorFactory;
import cm.aptoide.pt.database.accessors.StoreMinimalAdAccessor;
import cm.aptoide.pt.database.realm.Installed;
import cm.aptoide.pt.database.realm.MinimalAd;
import cm.aptoide.pt.database.realm.Rollback;
import cm.aptoide.pt.database.realm.StoredMinimalAd;
import cm.aptoide.pt.database.realm.Update;
import cm.aptoide.pt.dataprovider.util.DataproviderUtils;
import cm.aptoide.pt.logger.Logger;
import cm.aptoide.pt.utils.AptoideUtils;
import cm.aptoide.pt.v8engine.analytics.Analytics;
import cm.aptoide.pt.v8engine.repository.AdsRepository;
import cm.aptoide.pt.v8engine.repository.InstalledRepository;
import cm.aptoide.pt.v8engine.repository.RepositoryFactory;
import cm.aptoide.pt.v8engine.repository.RollbackRepository;
import cm.aptoide.pt.v8engine.repository.UpdateRepository;
import cm.aptoide.pt.v8engine.util.referrer.ReferrerUtils;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class InstalledIntentService extends IntentService {

  private static final String TAG = InstalledIntentService.class.getName();

  private final AdsRepository adsRepository;
  private final RollbackRepository repository;
  private final InstalledRepository installedRepository;
  private final UpdateRepository updatesRepository;
  private final CompositeSubscription subscriptions;

  public InstalledIntentService() {
    this("InstalledIntentService");
  }

  /**
   * Creates an IntentService.  Invoked by your subclass's constructor.
   *
   * @param name Used to name the worker thread, important only for debugging.
   */
  public InstalledIntentService(String name) {
    super(name);

    adsRepository = new AdsRepository();
    repository = RepositoryFactory.getRollbackRepository();
    installedRepository = RepositoryFactory.getInstalledRepository();
    updatesRepository = RepositoryFactory.getUpdateRepository();

    subscriptions = new CompositeSubscription();
  }

  @Override protected void onHandleIntent(Intent intent) {

    final String action = intent.getAction();
    final String packageName = intent.getData().getEncodedSchemeSpecificPart();

    confirmAction(packageName, action);

    if (!intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) && intent.getBooleanExtra(
        Intent.EXTRA_REPLACING, false)) {
      return;
    }
    Logger.d(TAG, "Action : " + action);

    switch (action) {
      case Intent.ACTION_PACKAGE_ADDED:
        onPackageAdded(packageName);
        break;
      case Intent.ACTION_PACKAGE_REPLACED:
        onPackageReplaced(packageName);
        break;
      case Intent.ACTION_PACKAGE_REMOVED:
        onPackageRemoved(packageName);
        break;
    }
  }

  private boolean shouldConfirmRollback(Rollback rollback, String action) {
    return rollback != null && ((rollback.getAction().equals(Rollback.Action.INSTALL.name())
        && action.equals(Intent.ACTION_PACKAGE_ADDED))
        || (rollback.getAction()
        .equals(Rollback.Action.UNINSTALL.name()) && action.equals(Intent.ACTION_PACKAGE_REMOVED))
        || (rollback.getAction().equals(Rollback.Action.UPDATE.name()) && action.equals(
        Intent.ACTION_PACKAGE_REPLACED))
        || (rollback.getAction().equals(Rollback.Action.DOWNGRADE.name()) && action.equals(
        Intent.ACTION_PACKAGE_ADDED)));
  }

  protected void onPackageAdded(String packageName) {
    Logger.d(TAG, "Package added: " + packageName);

    databaseOnPackageAdded(packageName);
    checkAndBroadcastReferrer(packageName);
  }

  private void checkAndBroadcastReferrer(String packageName) {
    StoreMinimalAdAccessor storeMinimalAdAccessor =
        AccessorFactory.getAccessorFor(StoredMinimalAd.class);
    Subscription unManagedSubscription =
        storeMinimalAdAccessor.get(packageName).subscribe(storeMinimalAd -> {
          if (storeMinimalAd != null) {
            ReferrerUtils.broadcastReferrer(packageName, storeMinimalAd.getReferrer());
            DataproviderUtils.AdNetworksUtils.knockCpi(storeMinimalAd);
            storeMinimalAdAccessor.remove(storeMinimalAd);
          } else {
            adsRepository.getAdsFromSecondInstall(packageName)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(
                    minimalAd -> ReferrerUtils.extractReferrer(minimalAd, ReferrerUtils.RETRIES,
                        true))
                .onErrorReturn(throwable1 -> new MinimalAd())
                .subscribe();
          }
        }, err -> {
          Logger.e(TAG, err);
          CrashReports.logException(err);
        });

    subscriptions.add(unManagedSubscription);
  }

  protected void onPackageReplaced(String packageName) {
    Logger.d(TAG, "Packaged replaced: " + packageName);
    databaseOnPackageReplaced(packageName);
  }

  protected void onPackageRemoved(String packageName) {
    Logger.d(TAG, "Packaged removed: " + packageName);
    databaseOnPackageRemoved(packageName);
  }

  private void databaseOnPackageAdded(String packageName) {
    PackageInfo packageInfo = AptoideUtils.SystemU.getPackageInfo(packageName);

    if (checkAndLogNullPackageInfo(packageInfo, packageName)) {
      return;
    }
    installedRepository.insert(new Installed(packageInfo));
  }

  private void databaseOnPackageReplaced(String packageName) {
    final Update update = updatesRepository.get(packageName).doOnError(throwable -> {
      Logger.e(TAG, throwable);
      CrashReports.logException(throwable);
    }).onErrorReturn(throwable -> null).toBlocking().first();

    if (update != null && update.getPackageName() != null && update.getTrustedBadge() != null) {
      Analytics.ApplicationInstall.replaced(packageName, update.getTrustedBadge());
    }

    PackageInfo packageInfo = AptoideUtils.SystemU.getPackageInfo(packageName);

    if (checkAndLogNullPackageInfo(packageInfo, packageName)) {
      return;
    }

    if (update != null) {
      if (packageInfo.versionCode >= update.getVersionCode()) {
        updatesRepository.remove(update).doOnError(throwable -> {
          Logger.e(TAG, throwable);
          CrashReports.logException(throwable);
        }).toBlocking().first();
      }
    }

    installedRepository.insert(new Installed(packageInfo));
  }

  /**
   * @param packageInfo packageInfo.
   * @return true if packageInfo is null, false otherwise.
   */
  private boolean checkAndLogNullPackageInfo(PackageInfo packageInfo, String packageName) {
    if (packageInfo == null) {
      CrashReports.logException(
          new IllegalArgumentException("PackageName null for package " + packageName));
      return true;
    } else {
      return false;
    }
  }

  private void databaseOnPackageRemoved(String packageName) {
    installedRepository.remove(packageName);
    updatesRepository.remove(packageName);
  }

  private void confirmAction(String packageName, String action) {
    repository.getNotConfirmedRollback(packageName)
        .first()
        .filter(rollback -> shouldConfirmRollback(rollback, action))
        .subscribe(rollback -> {
          repository.confirmRollback(rollback);
        }, throwable -> throwable.printStackTrace());
  }
}
