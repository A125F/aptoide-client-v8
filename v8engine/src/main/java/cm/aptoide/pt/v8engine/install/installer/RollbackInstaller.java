/*
 * Copyright (c) 2016.
 * Modified by Marcelo Benites on 29/09/2016.
 */

package cm.aptoide.pt.v8engine.install.installer;

import android.content.Context;
import cm.aptoide.pt.database.realm.Rollback;
import cm.aptoide.pt.v8engine.install.Installer;
import cm.aptoide.pt.v8engine.install.provider.RollbackFactory;
import cm.aptoide.pt.v8engine.repository.RollbackRepository;
import rx.Completable;
import rx.Observable;

/**
 * Created by trinkes on 9/8/16.
 */

public class RollbackInstaller implements Installer {

  private final DefaultInstaller defaultInstaller;
  private final RollbackRepository repository;
  private final RollbackFactory rollbackFactory;
  private final InstallationProvider installationProvider;

  public RollbackInstaller(DefaultInstaller defaultInstaller, RollbackRepository repository,
      RollbackFactory rollbackFactory, InstallationProvider installationProvider) {
    this.defaultInstaller = defaultInstaller;
    this.repository = repository;
    this.rollbackFactory = rollbackFactory;
    this.installationProvider = installationProvider;
  }

  @Override public Observable<Boolean> isInstalled(String md5) {
    return defaultInstaller.isInstalled(md5);
  }

  @Override public Completable install(Context context, String md5) {
    return installationProvider.getInstallation(md5)
        .cast(RollbackInstallation.class)
        .first()
        .toSingle()
        .flatMapCompletable(installation -> saveRollback(installation, Rollback.Action.INSTALL))
        .andThen(defaultInstaller.install(context, md5));
  }

  @Override public Completable update(Context context, String md5) {
    return installationProvider.getInstallation(md5)
        .cast(RollbackInstallation.class)
        .first()
        .toSingle()
        .flatMapCompletable(installation -> saveRollback(context, installation.getPackageName(),
            Rollback.Action.UPDATE, installation.getIcon(), installation.getVersionName()))
        .andThen(defaultInstaller.update(context, md5));
  }

  @Override public Completable downgrade(Context context, String md5) {
    return installationProvider.getInstallation(md5)
        .cast(RollbackInstallation.class)
        .first()
        .toSingle()
        .flatMapCompletable(installation -> saveRollback(context, installation.getPackageName(),
            Rollback.Action.DOWNGRADE, installation.getIcon(), installation.getVersionName()))
        .andThen(defaultInstaller.downgrade(context, md5));
  }

  @Override public Completable uninstall(Context context, String packageName, String versionName) {
    return saveRollback(context, packageName, Rollback.Action.UNINSTALL, null, versionName).andThen(
        defaultInstaller.uninstall(context, packageName, versionName));
  }

  private Completable saveRollback(Context context, String packageName, Rollback.Action action,
      String icon, String versionName) {
    return rollbackFactory.createRollback(context, packageName, action, icon, versionName)
        .doOnNext(rollback -> repository.save(rollback))
        .toCompletable();
  }

  private Completable saveRollback(RollbackInstallation installation, Rollback.Action action) {
    return Completable.fromAction(
        () -> repository.save(rollbackFactory.createRollback(installation, action)));
  }
}
