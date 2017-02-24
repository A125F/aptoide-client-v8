package cm.aptoide.pt.model.v7.store;

import cm.aptoide.pt.model.v7.BaseV7Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by trinkes on 23/02/2017.
 */
@Data @EqualsAndHashCode(callSuper = true) public class GetHomeMeta extends BaseV7Response {
  Data data;

  @lombok.Data public static class Data {
    Store store;
    HomeUser user;
  }
}
