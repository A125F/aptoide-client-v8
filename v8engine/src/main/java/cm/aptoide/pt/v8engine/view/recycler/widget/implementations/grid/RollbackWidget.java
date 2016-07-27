/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 27/07/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.widget.implementations.grid;

import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cm.aptoide.pt.actions.PermissionManager;
import cm.aptoide.pt.actions.PermissionRequest;
import cm.aptoide.pt.database.Database;
import cm.aptoide.pt.database.realm.Download;
import cm.aptoide.pt.database.realm.Rollback;
import cm.aptoide.pt.downloadmanager.AptoideDownloadManager;
import cm.aptoide.pt.downloadmanager.DownloadServiceHelper;
import cm.aptoide.pt.imageloader.ImageLoader;
import cm.aptoide.pt.logger.Logger;
import cm.aptoide.pt.utils.AptoideUtils;
import cm.aptoide.pt.utils.ShowMessage;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.util.DownloadFactory;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.RollbackDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.widget.Widget;
import io.realm.Realm;
import lombok.Cleanup;

/**
 * Created by sithengineer on 14/06/16.
 */
public class RollbackWidget extends Widget<RollbackDisplayable> {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

	private static final String TAG = RollbackWidget.class.getSimpleName();

	private ImageView appIcon;
	private TextView appName;
	private TextView appUpdateVersion;
	private TextView appState;
	private TextView rollbackAction;

	public RollbackWidget(View itemView) {
		super(itemView);
	}

	@Override
	protected void assignViews(View itemView) {
		appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
		appName = (TextView) itemView.findViewById(R.id.app_name);
		appState = (TextView) itemView.findViewById(R.id.app_state);
		appUpdateVersion = (TextView) itemView.findViewById(R.id.app_update_version);
		rollbackAction = (TextView) itemView.findViewById(R.id.ic_action);
	}

	@Override
	public void bindView(RollbackDisplayable displayable) {
		final Rollback pojo = displayable.getPojo();

		ImageLoader.load(pojo.getIcon(), appIcon);
		appName.setText(pojo.getAppName());
		appUpdateVersion.setText(pojo.getVersionName());
		appState.setText(
			String.format(
				getContext().getString(R.string.rollback_updated_at),
				DATE_FORMAT.format(new Date(pojo.getTimestamp()))
			)
		);

		rollbackAction.setOnClickListener( view -> {

			AptoideUtils.ThreadU.runOnIoThread(() -> {
				@Cleanup
				Realm realm = Database.get();
				Database.RollbackQ.upadteRollbackWithAction(realm, pojo.getPackageName(), Rollback.Action.UPDATE);
			});

			final Context context = view.getContext();
			ContextWrapper contextWrapper = (ContextWrapper) context;
			final PermissionRequest permissionRequest = ((PermissionRequest) contextWrapper.getBaseContext());

			permissionRequest.requestAccessToExternalFileSystem(() -> {
				ShowMessage.asSnack(view, R.string.downgrading_msg);

				DownloadFactory factory = new DownloadFactory();
				Download appDownload = factory.create(pojo);
				DownloadServiceHelper downloadServiceHelper = new DownloadServiceHelper(AptoideDownloadManager.getInstance(), new PermissionManager());
				downloadServiceHelper.startDownload(permissionRequest, appDownload).subscribe(download -> {
					if (download.getOverallDownloadStatus() == Download.COMPLETED) {
						//final String packageName = app.getPackageName();
						//final FileToDownload downloadedFile = download.getFilesToDownload().get(0);
						displayable.downgrade(getContext()).subscribe();
					}
				});
			}, () -> {
				Logger.e(TAG, "unable to access to external FS");
			});
		});
	}

	@Override
	public void onViewAttached() {

	}

	@Override
	public void onViewDetached() {

	}
}
