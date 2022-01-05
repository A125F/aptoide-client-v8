package cm.aptoide.pt.view.recycler;

import android.content.Context;
import android.view.View;
import androidx.annotation.CallSuper;
import androidx.recyclerview.widget.RecyclerView;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public abstract class RecyclerViewHolder<T> extends RecyclerView.ViewHolder {

  private final CompositeSubscription compositeSubscription = new CompositeSubscription();
  private T viewModel;
  private final Context context;

  protected RecyclerViewHolder(View itemView) {
    super(itemView);
    context = itemView.getContext();
  }

  /**
   * Updates this view with the received view model.
   *
   * @param viewModel the new view model
   */
  public final void updateViewModel(T viewModel) {
    this.viewModel = viewModel;
    update(context, viewModel);
  }

  protected abstract void update(Context context, T viewModel);

  protected T getViewModel() {
    return viewModel;
  }

  @CallSuper protected void addSubscription(Subscription s) {
    compositeSubscription.add(s);
  }

  public final void releaseSubscriptions() {
    if (compositeSubscription.hasSubscriptions() && !compositeSubscription.isUnsubscribed()) {
      compositeSubscription.unsubscribe();
    }
  }

  public abstract int getViewResource();
}
