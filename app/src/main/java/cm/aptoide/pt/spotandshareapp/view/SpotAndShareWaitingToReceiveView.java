package cm.aptoide.pt.spotandshareapp.view;

import cm.aptoide.pt.presenter.View;
import rx.Observable;

/**
 * Created by filipe on 12-06-2017.
 */

public interface SpotAndShareWaitingToReceiveView extends View {

  void finish();

  Observable<Void> startSearch();

  void openSpotandShareTransferRecordFragment();

  Observable<Void> backButtonEvent();

  void showExitWarning();

  Observable<Void> exitEvent();

  void navigateBack();

  void onLeaveGroupError();

  void joinGroup();
}
