package cm.aptoide.pt.editorialList;

import android.content.SharedPreferences;
import cm.aptoide.pt.dataprovider.exception.NoNetworkConnectionException;
import cm.aptoide.pt.dataprovider.interfaces.TokenInvalidator;
import cm.aptoide.pt.dataprovider.model.v7.DataList;
import cm.aptoide.pt.dataprovider.ws.BodyInterceptor;
import cm.aptoide.pt.dataprovider.ws.v7.BaseBody;
import cm.aptoide.pt.dataprovider.ws.v7.EditorialListData;
import cm.aptoide.pt.dataprovider.ws.v7.EditorialListRequest;
import cm.aptoide.pt.dataprovider.ws.v7.EditorialListResponse;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import rx.Single;

public class EditorialCardListService {

  private final BodyInterceptor<BaseBody> bodyInterceptorPoolV7;
  private final OkHttpClient okHttpClient;
  private final TokenInvalidator tokenInvalidator;
  private final Converter.Factory converterFactory;
  private final SharedPreferences sharedPreferences;
  private final int limit;
  private boolean loading;

  public EditorialCardListService(BodyInterceptor<BaseBody> bodyInterceptorPoolV7,
      OkHttpClient okHttpClient, TokenInvalidator tokenInvalidator,
      Converter.Factory converterFactory, SharedPreferences sharedPreferences, int limit) {

    this.bodyInterceptorPoolV7 = bodyInterceptorPoolV7;
    this.okHttpClient = okHttpClient;
    this.tokenInvalidator = tokenInvalidator;
    this.converterFactory = converterFactory;
    this.sharedPreferences = sharedPreferences;
    this.limit = limit;
  }

  public Single<EditorialCardListModel> loadEditorialCardListModel(int offset,
      boolean invalidateCache) {
    if (loading) {
      return Single.just(new EditorialCardListModel(true));
    }
    return EditorialListRequest.of(bodyInterceptorPoolV7, okHttpClient, converterFactory,
        tokenInvalidator, sharedPreferences, limit, offset)
        .observe(invalidateCache, false)
        .doOnSubscribe(() -> loading = true)
        .doOnUnsubscribe(() -> loading = false)
        .doOnTerminate(() -> loading = false)
        .map(this::mapEditorialCardList)
        .toSingle()
        .onErrorReturn(this::mapEditorialCardListError);
  }

  private EditorialCardListModel mapEditorialCardListError(Throwable throwable) {
    if (throwable instanceof NoNetworkConnectionException
        || throwable instanceof ConnectException) {
      return new EditorialCardListModel(EditorialCardListModel.Error.NETWORK);
    } else {
      return new EditorialCardListModel(EditorialCardListModel.Error.GENERIC);
    }
  }

  private EditorialCardListModel mapEditorialCardList(EditorialListResponse actionItemResponse) {
    DataList<EditorialListData> dataList = actionItemResponse.getDataList();
    List<EditorialListData> items = dataList.getList();
    List<CurationCard> curationCards = buildCurationCardList(items);
    return new EditorialCardListModel(curationCards, dataList.getNext(), dataList.getTotal());
  }

  private List<CurationCard> buildCurationCardList(List<EditorialListData> items) {
    List<CurationCard> curationCards = new ArrayList<>();
    for (EditorialListData actionItemData : items) {
      CurationCard curationCard =
          new CurationCard(actionItemData.getId(), actionItemData.getCaption(),
              actionItemData.getIcon(), actionItemData.getTitle(), actionItemData.getViews(),
              actionItemData.getType(), actionItemData.getDate(),
              actionItemData.getAppearance() != null ? actionItemData.getAppearance()
                  .getCaption()
                  .getTheme() : "",
              actionItemData.getFlair() != null ? actionItemData.getFlair() : "");
      curationCards.add(curationCard);
    }
    return curationCards;
  }
}
