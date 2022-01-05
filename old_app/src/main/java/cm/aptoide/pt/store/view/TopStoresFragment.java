package cm.aptoide.pt.store.view;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import cm.aptoide.analytics.AnalyticsManager;
import cm.aptoide.analytics.implementation.navigation.NavigationTracker;
import cm.aptoide.analytics.implementation.navigation.ScreenTagHistory;
import cm.aptoide.pt.R;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.dataprovider.interfaces.SuccessRequestListener;
import cm.aptoide.pt.dataprovider.model.v7.store.ListStores;
import cm.aptoide.pt.dataprovider.model.v7.store.Store;
import cm.aptoide.pt.dataprovider.ws.v7.Endless;
import cm.aptoide.pt.dataprovider.ws.v7.store.ListStoresRequest;
import cm.aptoide.pt.store.StoreAnalytics;
import cm.aptoide.pt.view.fragment.GridRecyclerFragmentWithDecorator;
import cm.aptoide.pt.view.recycler.BaseAdapter;
import cm.aptoide.pt.view.recycler.EndlessRecyclerOnScrollListener;
import cm.aptoide.pt.view.recycler.displayable.Displayable;
import com.trello.rxlifecycle.android.FragmentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by trinkes on 8/25/16.
 */
public class TopStoresFragment extends GridRecyclerFragmentWithDecorator<BaseAdapter>
    implements Endless {

  public static final int STORES_LIMIT_PER_REQUEST = 10;
  public static String TAG = TopStoresFragment.class.getSimpleName();
  @Inject AnalyticsManager analyticsManager;
  @Inject NavigationTracker navigationTracker;
  private int offset = 0;
  private StoreAnalytics storeAnalytics;
  private final SuccessRequestListener<ListStores> listener =
      listStores -> Observable.fromCallable(() -> createDisplayables(listStores))
          .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
          .subscribe(displayables -> addDisplayables(displayables), err -> {
            CrashReport.getInstance()
                .log(err);
          });

  public static TopStoresFragment newInstance() {
    return new TopStoresFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getFragmentComponent(savedInstanceState).inject(this);
    storeAnalytics = new StoreAnalytics(analyticsManager, navigationTracker);
    setHasOptionsMenu(true);
  }

  @Override public ScreenTagHistory getHistoryTracker() {
    return ScreenTagHistory.Builder.build(this.getClass()
        .getSimpleName());
  }

  @NonNull private List<Displayable> createDisplayables(ListStores listStores) {
    List<Displayable> displayables = new ArrayList<>();
    for (final Store store : listStores.getDataList()
        .getList()) {
      displayables.add(
          new GridStoreDisplayable(store, "Add Store Dialog Top Stores", storeAnalytics));
    }
    return displayables;
  }

  @Override public int getContentViewId() {
    return R.layout.fragment_with_toolbar_no_theme;
  }

  @Override public void load(boolean create, boolean refresh, Bundle savedInstanceState) {
    super.load(create, refresh, savedInstanceState);
    fetchStores();
  }

  @Override public void setupViews() {
    super.setupViews();
    setupToolbar();
  }

  private void fetchStores() {
    final ListStoresRequest listStoresRequest =
        requestFactoryCdnPool.newListStoresRequest(offset, STORES_LIMIT_PER_REQUEST);
    EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener =
        new EndlessRecyclerOnScrollListener(this.getAdapter(), listStoresRequest, listener,
            err -> err.printStackTrace());
    getRecyclerView().addOnScrollListener(endlessRecyclerOnScrollListener);
    endlessRecyclerOnScrollListener.onLoadMore(false, false);
  }

  @Override protected boolean displayHomeUpAsEnabled() {
    return true;
  }

  @Override public void setupToolbarDetails(Toolbar toolbar) {
    toolbar.setTitle(R.string.top_stores_fragment_title);
    toolbar.setLogo(R.drawable.logo_toolbar);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      getActivity().onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public int getOffset() {
    return offset;
  }

  @Override public void setOffset(int offset) {
    this.offset = offset;
  }

  @Override public Integer getLimit() {
    return STORES_LIMIT_PER_REQUEST;
  }
}
