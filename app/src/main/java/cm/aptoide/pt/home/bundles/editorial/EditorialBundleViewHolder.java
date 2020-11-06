package cm.aptoide.pt.home.bundles.editorial;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import cm.aptoide.aptoideviews.appcoins.BonusAppcView;
import cm.aptoide.aptoideviews.skeleton.Skeleton;
import cm.aptoide.aptoideviews.skeleton.SkeletonUtils;
import cm.aptoide.pt.R;
import cm.aptoide.pt.bonus.BonusAppcModel;
import cm.aptoide.pt.editorial.CaptionBackgroundPainter;
import cm.aptoide.pt.editorialList.CurationCard;
import cm.aptoide.pt.home.bundles.base.ActionBundle;
import cm.aptoide.pt.home.bundles.base.ActionItem;
import cm.aptoide.pt.home.bundles.base.EditorialActionBundle;
import cm.aptoide.pt.home.bundles.base.HomeBundle;
import cm.aptoide.pt.home.bundles.base.HomeEvent;
import cm.aptoide.pt.networking.image.ImageLoader;
import cm.aptoide.pt.reactions.ReactionsHomeEvent;
import cm.aptoide.pt.reactions.TopReactionsPreview;
import cm.aptoide.pt.reactions.data.TopReaction;
import cm.aptoide.pt.reactions.ui.ReactionsPopup;
import cm.aptoide.pt.themes.ThemeManager;
import com.google.android.material.snackbar.Snackbar;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import rx.subjects.PublishSubject;

import static cm.aptoide.pt.editorial.ViewsFormatter.formatNumberOfViews;
import static cm.aptoide.pt.reactions.ReactionMapper.mapReaction;
import static cm.aptoide.pt.reactions.ReactionMapper.mapUserReaction;

/**
 * Created by franciscocalado on 29/08/2018.
 */

public class EditorialBundleViewHolder extends EditorialViewHolder {
  private final PublishSubject<HomeEvent> uiEventsListener;
  private final View editorialCard;
  private final TextView editorialTitle;
  private final TextView editorialDate;
  private final ImageView backgroundImage;
  private final TextView editorialViews;
  private final ImageButton reactButton;
  private final CardView curationTypeCaption;
  private final TextView curationTypeCaptionText;
  private final BonusAppcView bonusAppcView;
  private final CaptionBackgroundPainter captionBackgroundPainter;
  private final ThemeManager themeAttributeProvider;
  private TopReactionsPreview topReactionsPreview;
  private Skeleton skeleton;

  public EditorialBundleViewHolder(View view, PublishSubject<HomeEvent> uiEventsListener,
      CaptionBackgroundPainter captionBackgroundPainter, ThemeManager themeAttributeProvider) {
    super(view);
    this.uiEventsListener = uiEventsListener;
    this.editorialCard = view.findViewById(R.id.editorial_card);
    this.editorialTitle = view.findViewById(R.id.editorial_title);
    this.editorialDate = view.findViewById(R.id.editorial_date);
    this.editorialViews = view.findViewById(R.id.editorial_views);
    this.backgroundImage = view.findViewById(R.id.background_image);
    this.reactButton = view.findViewById(R.id.add_reactions);
    this.curationTypeCaption = view.findViewById(R.id.curation_type_bubble);
    this.curationTypeCaptionText = view.findViewById(R.id.curation_type_bubble_text);
    this.bonusAppcView = view.findViewById(R.id.bonus_appc_view);
    this.captionBackgroundPainter = captionBackgroundPainter;
    this.themeAttributeProvider = themeAttributeProvider;
    topReactionsPreview = new TopReactionsPreview();
    topReactionsPreview.initialReactionsSetup(view);

    skeleton = SkeletonUtils.applySkeleton(editorialCard, view.findViewById(R.id.root_cardview),
        R.layout.editorial_action_item_skeleton);
  }

  @Override public void setBundle(HomeBundle homeBundle, int position) {
    ActionBundle actionBundle = (ActionBundle) homeBundle;
    ActionItem actionItem = actionBundle.getActionItem();
    boolean hasBonus = false;
    int bonusValue = 0;
    if (actionBundle instanceof EditorialActionBundle) {
      EditorialActionBundle editorialActionBundle = ((EditorialActionBundle) actionBundle);
      BonusAppcModel bonusAppcModel = editorialActionBundle.getBonusAppcModel();
      hasBonus = bonusAppcModel.getHasBonusAppc();
      bonusValue = bonusAppcModel.getBonusPercentage();
    }
    if (actionItem == null) {
      skeleton.showSkeleton();
    } else {
      skeleton.showOriginal();
      setBundleInformation(actionItem.getIcon(), actionItem.getTitle(), actionItem.getSubTitle(),
          actionItem.getCardId(), actionItem.getNumberOfViews(), actionItem.getType(),
          actionItem.getDate(), getAdapterPosition(), homeBundle, actionItem.getReactionList(),
          actionItem.getTotal(), actionItem.getUserReaction(), actionItem.getCaptionColor(),
          actionItem.getFlair(), hasBonus, bonusValue);
    }
  }

  private void setBundleInformation(String icon, String title, String subTitle, String cardId,
      String numberOfViews, String type, String date, int position, HomeBundle homeBundle,
      List<TopReaction> reactions, int numberOfReactions, String userReaction, String captionColor,
      String flair, boolean hasBonusAppc, int bonusPercentage) {
    clearReactions();
    setReactions(reactions, numberOfReactions, userReaction);
    ImageLoader.with(itemView.getContext())
        .load(icon, backgroundImage);
    editorialTitle.setText(title);
    editorialViews.setText(String.format(itemView.getContext()
            .getString(R.string.editorial_card_short_number_views),
        formatNumberOfViews(numberOfViews)));
    curationTypeCaptionText.setText(subTitle);
    captionBackgroundPainter.addColorBackgroundToCaption(curationTypeCaption, captionColor);
    setupCalendarDateString(date);
    reactButton.setOnClickListener(view -> uiEventsListener.onNext(
        new EditorialHomeEvent(cardId, type, homeBundle, position,
            HomeEvent.Type.REACT_SINGLE_PRESS)));
    reactButton.setOnLongClickListener(view -> {
      uiEventsListener.onNext(new EditorialHomeEvent(cardId, type, homeBundle, position,
          HomeEvent.Type.REACT_LONG_PRESS));
      return true;
    });
    editorialCard.setOnClickListener(view -> uiEventsListener.onNext(
        new EditorialHomeEvent(cardId, type, homeBundle, position, HomeEvent.Type.EDITORIAL)));

    if (hasBonusAppc) {
      setFlair(flair, bonusPercentage);
    } else {
      bonusAppcView.setVisibility(View.GONE);
    }
  }

  private void setFlair(String flair, int bonusPercentage) {
    if (flair.equals("appc-bonus-25")) {
      bonusAppcView.setVisibility(View.VISIBLE);
      bonusAppcView.setPercentage(bonusPercentage);
    } else {
      bonusAppcView.setVisibility(View.GONE);
    }
  }

  private void setupCalendarDateString(String date) {
    String[] dateSplitted = date.split(" ");
    String newFormatDate = dateSplitted[0].replace("-", "/");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    Date newDate = null;
    String formattedDate;
    try {
      newDate = dateFormat.parse(newFormatDate);
    } catch (ParseException parseException) {
      Snackbar.make(editorialCard, itemView.getContext()
          .getString(R.string.unknown_error), Snackbar.LENGTH_SHORT)
          .show();
    }
    if (newDate != null) {
      formattedDate = DateFormat.getDateInstance(DateFormat.SHORT)
          .format(newDate);
      editorialDate.setText(formattedDate);
    }
  }

  private void setReactions(List<TopReaction> reactions, int numberOfReactions,
      String userReaction) {
    setUserReaction(userReaction);
    topReactionsPreview.setReactions(reactions, numberOfReactions, itemView.getContext());
  }

  public void setEditorialCard(CurationCard curationCard, int position,
      BonusAppcModel bonusAppcModel) {
    skeleton.showOriginal();
    setBundleInformation(curationCard.getIcon(), curationCard.getTitle(),
        curationCard.getSubTitle(), curationCard.getId(), curationCard.getViews(),
        curationCard.getType(), curationCard.getDate(), position, null, curationCard.getReactions(),
        curationCard.getNumberOfReactions(), curationCard.getUserReaction(),
        curationCard.getCaptionColor(), curationCard.getFlair(), bonusAppcModel.getHasBonusAppc(),
        bonusAppcModel.getBonusPercentage());
  }

  public void showReactions(String cardId, String groupId, int position) {
    ReactionsPopup reactionsPopup = new ReactionsPopup(itemView.getContext(), reactButton);
    reactionsPopup.show();
    reactionsPopup.setOnReactionsItemClickListener(item -> {
      uiEventsListener.onNext(
          new ReactionsHomeEvent(cardId, groupId, null, position, HomeEvent.Type.REACTION,
              mapUserReaction(item)));
      reactionsPopup.dismiss();
      reactionsPopup.setOnReactionsItemClickListener(null);
    });
    reactionsPopup.setOnDismissListener(item -> {
      uiEventsListener.onNext(
          new EditorialHomeEvent(cardId, groupId, null, position, HomeEvent.Type.POPUP_DISMISS));

      reactionsPopup.setOnDismissListener(null);
    });
  }

  private void setUserReaction(String reaction) {
    if (topReactionsPreview.isReactionValid(reaction)) {
      reactButton.setImageResource(mapReaction(reaction));
    } else {
      reactButton.setImageResource(
          themeAttributeProvider.getAttributeForTheme(R.attr.reactionInputDrawable).resourceId);
    }
  }

  private void clearReactions() {
    reactButton.setImageResource(
        themeAttributeProvider.getAttributeForTheme(R.attr.reactionInputDrawable).resourceId);
    topReactionsPreview.clearReactions();
  }
}