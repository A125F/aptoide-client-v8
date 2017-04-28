/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 02/09/2016.
 */

package cm.aptoide.pt.database.accessors;

import android.support.annotation.NonNull;
import cm.aptoide.pt.database.realm.Installed;
import cm.aptoide.pt.database.schedulers.RealmSchedulers;
import io.realm.Sort;
import java.util.List;
import rx.Completable;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by sithengineer on 01/09/16.
 */
public class InstalledAccessor extends SimpleAccessor<Installed> {

  public InstalledAccessor(Database db) {
    super(db, Installed.class);
  }

  public Observable<List<Installed>> getAllInstalled() {
    return database.getAll(Installed.class).flatMap(installs -> filterCompleted(installs));
  }

  public Observable<List<Installed>> getAllInstalledSorted() {
    return getAllInstalledSorted(Sort.ASCENDING);
  }

  public Observable<List<Installed>> getAllInstalledSorted(Sort sort) {
    return Observable.fromCallable(() -> Database.getInternal())
        .flatMap(realm -> realm.where(Installed.class)
            .findAllSorted(Installed.NAME, sort)
            .asObservable()
            .unsubscribeOn(RealmSchedulers.getScheduler()))
        .flatMap(installed -> database.copyFromRealm(installed))
        .subscribeOn(RealmSchedulers.getScheduler())
        .observeOn(Schedulers.io())
        .flatMap(installs -> filterCompleted(installs));
  }

  @NonNull private Observable<List<Installed>> filterCompleted(List<Installed> installs) {
    return Observable.from(installs)
        .filter(installed -> installed.getStatus() == Installed.STATUS_COMPLETED)
        .toList();
  }

  public Completable remove(String packageName, int versionCode) {
    return database.getRealm()
        .doOnNext(realm -> database.deleteObject(realm, realm.where(Installed.class)
            .equalTo(Installed.PACKAGE_NAME, packageName)
            .equalTo(Installed.VERSION_CODE, versionCode)
            .findFirst()))
        .observeOn(Schedulers.io())
        .toCompletable();
  }

  public Observable<Boolean> isInstalled(String packageName) {
    return getInstalled(packageName).map(
        installed -> installed != null && installed.getStatus() == Installed.STATUS_COMPLETED);
  }

  public Observable<Installed> getInstalled(String packageName) {
    return getInstalledAsList(packageName).map(installeds -> {
      if (installeds.isEmpty()) {
        return null;
      } else {
        return installeds.get(0);
      }
    });
  }

  public Observable<Installed> get(String packageName, int versionCode) {
    return Observable.fromCallable(() -> Database.getInternal())
        .map(realm -> realm.where(Installed.class)
            .equalTo(Installed.PACKAGE_NAME, packageName)
            .equalTo(Installed.VERSION_CODE, versionCode))
        .flatMap(installed -> database.findFirst(installed))
        .subscribeOn(RealmSchedulers.getScheduler());
  }

  public Observable<List<Installed>> getAsList(String packageName, int versionCode) {
    return Observable.fromCallable(() -> Database.getInternal())
        .flatMap(realm -> realm.where(Installed.class)
            .equalTo(Installed.PACKAGE_NAME, packageName)
            .equalTo(Installed.VERSION_CODE, versionCode)
            .findAll()
            .asObservable()
            .unsubscribeOn(RealmSchedulers.getScheduler()))
        .flatMap(installeds -> database.copyFromRealm(installeds))
        .subscribeOn(RealmSchedulers.getScheduler());
  }

  public Observable<List<Installed>> getInstalledAsList(String packageName) {
    return Observable.fromCallable(() -> Database.getInternal())
        .flatMap(realm -> realm.where(Installed.class)
            .equalTo(Installed.PACKAGE_NAME, packageName)
            .findAll()
            .asObservable()
            .unsubscribeOn(RealmSchedulers.getScheduler()))
        .flatMap(installeds -> database.copyFromRealm(installeds))
        .subscribeOn(RealmSchedulers.getScheduler())
        .flatMap(installs -> filterCompleted(installs));
  }

  public Observable<List<Installed>> getInstalled(String[] apps) {
    return Observable.fromCallable(() -> Database.getInternal())
        .flatMap(realm -> realm.where(Installed.class)
            .in(Installed.PACKAGE_NAME, apps)
            .findAll()
            .asObservable()
            .unsubscribeOn(RealmSchedulers.getScheduler()))
        .flatMap(installeds -> database.copyFromRealm(installeds))
        .subscribeOn(RealmSchedulers.getScheduler())
        .flatMap(installs -> filterCompleted(installs));
  }

  public void insertAll(List<Installed> installedList) {
    database.insertAll(installedList);
  }

  public void insert(Installed installed) {
    database.insert(installed);
  }

  public Observable<List<Installed>> getAllAsList(String packageName) {
    return Observable.fromCallable(() -> Database.getInternal())
        .flatMap(realm -> realm.where(Installed.class)
            .equalTo(Installed.PACKAGE_NAME, packageName)
            .findAll()
            .asObservable()
            .unsubscribeOn(RealmSchedulers.getScheduler()))
        .flatMap(installeds -> database.copyFromRealm(installeds))
        .subscribeOn(RealmSchedulers.getScheduler());
  }

  public Observable<Installed> get(String packageNameAndVersionCode) {
    return database.get(Installed.class, "packageAndVersionCode", packageNameAndVersionCode);
  }

}
