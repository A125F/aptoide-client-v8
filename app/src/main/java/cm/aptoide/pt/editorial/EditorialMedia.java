package cm.aptoide.pt.editorial;

/**
 * Created by D01 on 29/08/2018.
 */

public class EditorialMedia {

  private final String type;
  private final String description;
  private final String thumbnail;
  private final String url;

  public EditorialMedia(String type, String description, String thumbnail, String url) {
    this.type = type;
    this.description = description;
    this.thumbnail = thumbnail;
    this.url = url;
  }

  public String getType() {
    return type;
  }

  public boolean hasType() {
    return type != null && !type.equals("");
  }

  public boolean isImage() {
    return hasType() && type.equals("image");
  }

  public boolean isVideo() {
    return hasType() && type.equals("video");
  }

  public boolean isEmbedded() {
    return hasType() && type.equals("video_webview");
  }

  public String getDescription() {
    return description;
  }

  public boolean hasDescription() {
    return description != null && !description.equals("");
  }

  public String getUrl() {
    return url;
  }

  public boolean hasUrl() {
    return url != null && !url.equals("");
  }

  public String getThumbnail() {
    return thumbnail;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
