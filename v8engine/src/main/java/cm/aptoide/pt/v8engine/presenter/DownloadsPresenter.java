package cm.aptoide.pt.v8engine.presenter;

import android.content.Context;
import android.os.Bundle;
import cm.aptoide.pt.v8engine.InstallManager;
import cm.aptoide.pt.v8engine.InstallationProgress;
import cm.aptoide.pt.v8engine.crashreports.CrashReport;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DownloadsPresenter implements Presenter {

  private final DownloadsView view;
  private final InstallManager installManager;

  public DownloadsPresenter(DownloadsView downloadsView, InstallManager installManager) {
    this.view = downloadsView;
    this.installManager = installManager;
  }

  @Override public void present() {

    view.getLifecycle()
        .filter(lifecycleEvent -> lifecycleEvent == View.LifecycleEvent.RESUME)
        .first()
        .observeOn(Schedulers.computation())
        .flatMap(created -> installManager.getInstallations()
            .sample(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(downloads -> {
              if (downloads == null || downloads.isEmpty()) {
                view.showEmptyDownloadList();
                return Observable.empty();
              }
              return Observable.merge(setActive(downloads), setStandBy(downloads),
                  setCompleted(downloads));
            }))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
          // does nothing
        }, err -> {
          CrashReport.getInstance().log(err);
          view.showEmptyDownloadList();
        });
  }

  @Override public void saveState(Bundle state) {
  }

  @Override public void restoreState(Bundle state) {
  }

  private Observable<Void> setActive(List<InstallationProgress> downloads) {
    return Observable.from(downloads)
        .filter(d -> isInstalling(d))
        .toList()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(onGoingDownloads -> view.showActiveDownloads(onGoingDownloads))
        .map(__ -> null);
  }

  private Observable<Void> setStandBy(List<InstallationProgress> downloads) {
    return Observable.from(downloads)
        .filter(d -> isStandingBy(d))
        .toList()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(onGoingDownloads -> view.showStandByDownloads(onGoingDownloads))
        .map(__ -> null);
  }

  private Observable<Void> setCompleted(List<InstallationProgress> downloads) {
    return Observable.from(downloads)
        .filter(d -> !isInstalling(d) && !isStandingBy(d))
        .toList()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(onGoingDownloads -> view.showCompletedDownloads(onGoingDownloads))
        .map(__ -> null);
  }

  private boolean isInstalling(InstallationProgress progress) {
    return progress.isIndeterminate()
        || progress.getState() == InstallationProgress.InstallationStatus.INSTALLING;
  }

  private boolean isStandingBy(InstallationProgress progress) {
    return progress.getState() == InstallationProgress.InstallationStatus.FAILED
        || progress.getState()
        == InstallationProgress.InstallationStatus.PAUSED;
  }

  public void pauseInstall(Context context, DownloadsView.DownloadViewModel download) {
    installManager.stopInstallation(context, download.getAppMd5());
  }
}
