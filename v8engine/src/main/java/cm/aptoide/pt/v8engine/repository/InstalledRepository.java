package cm.aptoide.pt.v8engine.repository;

import cm.aptoide.pt.database.accessors.InstalledAccessor;
import cm.aptoide.pt.database.realm.Installed;
import cm.aptoide.pt.database.schedulers.RealmSchedulers;
import java.util.List;
import rx.Completable;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by marcelobenites on 7/27/16.
 */
public class InstalledRepository implements Repository<Installed, String> {

  private final InstalledAccessor accessor;

  InstalledRepository(InstalledAccessor accessor) {
    this.accessor = accessor;
  }

  /**
   * Get all installed apps
   * <dl>
   * <dt><b>Scheduler:</b></dt>
   * <dd>{@code getAll} operate by default on {@link RealmSchedulers}.</dd>
   * </dl>
   *
   * @return an observable with a list of installed apps
   */
  public Observable<List<Installed>> getAllInstalled() {
    return accessor.getAllInstalled();
  }

  public Observable<List<Installed>> getAsList(String packageName) {
    return accessor.getAllAsList(packageName);
  }

  public Observable<Installed> getAsList(String packageName, int versionCode) {
    return accessor.getAsList(packageName, versionCode)
        .observeOn(Schedulers.io())
        .map(installeds -> {
          if (installeds.isEmpty()) {
            return null;
          } else {
            return installeds.get(0);
          }
        });
  }

  @Override public void save(Installed installed) {
    accessor.insert(installed);
  }

  @Override public Observable<Installed> get(String packageNameAndVersionCode) {
    return accessor.get(packageNameAndVersionCode);
  }

  public Observable<Installed> getInstalled(String packageName) {
    return accessor.getInstalled(packageName);
  }

  public Completable remove(String packageName, int versionCode) {
    return accessor.remove(packageName, versionCode);
  }

  public Observable<Boolean> isInstalled(String packageName) {
    return accessor.isInstalled(packageName);
  }

  public Observable<List<Installed>> getAllInstalledSorted() {
    return accessor.getAllInstalledSorted();
  }

  public boolean contains(String packageName, int vercode) {
    Installed installed = get(packageName).toBlocking().first();
    return installed != null && installed.getVersionCode() == vercode;
  }

  public Observable<Installed> get(String packageName, int versionCode) {
    return accessor.get(packageName, versionCode);
  }

  public Observable<List<Installed>> getInstalled(String[] packageNames) {
    return accessor.getInstalled(packageNames);
  }
}
