package cm.aptoide.pt.shareapps.socket.entities;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;

/**
 * Created by neuro on 27-01-2017.
 */
@Data public class AndroidAppInfo implements Serializable {

  private FileInfo apk, mainObb, patchObb;
  private String appName;
  private String packageName;
  private String filePath;
  private String obbsFilePath;

  public AndroidAppInfo(String appName, String packageName, File apk, File mainObb, File patchObb) {
    this(appName, packageName, apk, mainObb);
    this.patchObb = new FileInfo(patchObb);
  }

  public AndroidAppInfo(String appName, String packageName, File apk, File mainObb) {
    this(appName, packageName, apk);
    this.mainObb = new FileInfo(mainObb);
  }

  public AndroidAppInfo(String appName, String packageName, File apk) {
    this.appName = appName;
    this.packageName = packageName;
    this.apk = new FileInfo(apk);
  }

  public List<String> getFilesPathsList() {
    List<String> list = new LinkedList<>();
    list.add(apk.getFilePath());

    if (hasMainObb()) {
      list.add(getMainObb().getFilePath());
    }

    if (hasPatchObb()) {
      list.add(getPatchObb().getFilePath());
    }

    return list;
  }

  public boolean hasMainObb() {
    return mainObb != null;
  }

  public boolean hasPatchObb() {
    return patchObb != null;
  }

  public List<FileInfo> getFileInfosList() {
    List<FileInfo> list = new LinkedList<>();
    list.add(apk);

    if (hasMainObb()) {
      list.add(getMainObb());
    }

    if (hasPatchObb()) {
      list.add(getPatchObb());
    }

    return list;
  }

  public List<FileInfo> getFiles() {
    List<FileInfo> fileInfos = new LinkedList<>();

    fileInfos.add(apk);

    if (hasMainObb()) {
      fileInfos.add(mainObb);
    }

    if (hasMainObb()) {
      fileInfos.add(patchObb);
    }

    return fileInfos;
  }

  public long getFilesSize() {
    long total = apk.getSize();

    if (hasMainObb()) {
      total += mainObb.getSize();
    }

    if (hasPatchObb()) {
      total += patchObb.getSize();
    }

    return total;
  }
}
