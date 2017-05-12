package cm.aptoide.pt.database.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by trinkes on 10/05/2017.
 */

public class Notification extends RealmObject {
  @PrimaryKey
  private String key;
  private String abTestingGroup;
  private String body;
  private int campaignId;
  private String img;
  private String lang;
  private String title;
  private String url;
  private String urlTrack;
  private int type;
  private long timeStamp;
  private boolean showed;

  public Notification(String abTestingGroup, String body, int campaignId, String img, String lang,
      String title, String url, String urlTrack, long timeStamp, int type, boolean showed) {
    this.body = body;
    this.img = img;
    this.title = title;
    this.url = url;
    this.type = type;
    this.abTestingGroup = abTestingGroup;
    this.campaignId = campaignId;
    this.lang = lang;
    this.urlTrack = urlTrack;
    this.timeStamp = timeStamp;
    this.showed = showed;
    key = String.valueOf(timeStamp + type);
  }

  public Notification() {
  }

  public boolean isShowed() {
    return showed;
  }

  public int getType() {
    return type;
  }

  public String getAbTestingGroup() {
    return abTestingGroup;
  }

  public String getBody() {
    return body;
  }

  public int getCampaignId() {
    return campaignId;
  }

  public String getImg() {
    return img;
  }

  public String getLang() {
    return lang;
  }

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }

  public String getUrlTrack() {
    return urlTrack;
  }

  public long getTimeStamp() {
    return timeStamp;
  }
}
