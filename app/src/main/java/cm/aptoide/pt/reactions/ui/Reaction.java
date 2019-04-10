package cm.aptoide.pt.reactions.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import cm.aptoide.pt.reactions.data.ReactionType;
import com.airbnb.lottie.LottieAnimationView;

public class Reaction {

  private final LottieAnimationView view;
  private final ReactionType reactionType;

  public Reaction(final ReactionType reactionType, Context context) {
    this.reactionType = reactionType;
    view = new LottieAnimationView(context);
    view.setAnimation(this.reactionType.name()
        .toLowerCase() + ".json");
    view.loop(true);
    view.setPadding(8, 8, 8, 8);
  }

  public void setReactionParams(ViewGroup.LayoutParams params) {
    view.setLayoutParams(params);
  }

  public void play() {
    view.playAnimation();
  }

  public LottieAnimationView getView() {
    return view;
  }

  public void cancel() {
    view.cancelAnimation();
  }

  public void setCallback(final ReactionsView.Callback callback) {
    view.setOnClickListener(view -> {
      if (callback != null) {
        callback.onReactionItemClicked(reactionType);
      }
    });
  }
}