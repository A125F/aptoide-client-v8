package cm.aptoide.pt.dataprovider.ws.v7.analyticsbody;

import cm.aptoide.pt.dataprovider.ws.v7.BaseBody;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by trinkes on 30/12/2016.
 */
public class AnalyticsBaseBody extends BaseBody {

  private final String aptoidePackage;

  public AnalyticsBaseBody(String aptoidePackage) {
    this.aptoidePackage = aptoidePackage;
  }

  public String getAptoidePackage() {
    return aptoidePackage;
  }
}
