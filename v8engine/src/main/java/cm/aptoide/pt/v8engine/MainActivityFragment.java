/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 15/07/2016.
 */

package cm.aptoide.pt.v8engine;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import cm.aptoide.pt.actions.PermissionManager;
import cm.aptoide.pt.dataprovider.ws.v7.listapps.StoreUtils;
import cm.aptoide.pt.dataprovider.ws.v7.store.StoreContext;
import cm.aptoide.pt.downloadmanager.AptoideDownloadManager;
import cm.aptoide.pt.downloadmanager.DownloadServiceHelper;
import cm.aptoide.pt.model.v7.Event;
import cm.aptoide.pt.preferences.managed.ManagerPreferences;
import cm.aptoide.pt.preferences.secure.SecurePreferences;
import cm.aptoide.pt.utils.AptoideUtils;
import cm.aptoide.pt.utils.ShowMessage;
import cm.aptoide.pt.v8engine.activities.AptoideSimpleFragmentActivity;
import cm.aptoide.pt.v8engine.analytics.Analytics;
import cm.aptoide.pt.v8engine.fragment.BaseWizardViewerFragment;
import cm.aptoide.pt.v8engine.fragment.implementations.HomeFragment;
import cm.aptoide.pt.v8engine.install.InstallManager;
import cm.aptoide.pt.v8engine.install.download.DownloadInstallationProvider;
import cm.aptoide.pt.v8engine.interfaces.DrawerFragment;
import cm.aptoide.pt.v8engine.interfaces.FragmentShower;
import cm.aptoide.pt.v8engine.services.PullingContentService;
import cm.aptoide.pt.v8engine.util.DownloadFactory;
import cm.aptoide.pt.v8engine.util.FragmentUtils;
import cm.aptoide.pt.v8engine.util.StoreUtilsProxy;

/**
 * Created by neuro on 06-05-2016.
 */
public class MainActivityFragment extends AptoideSimpleFragmentActivity implements FragmentShower {

	@Override
	protected android.support.v4.app.Fragment createFragment() {
		return HomeFragment.newInstance(V8Engine.getConfiguration().getDefaultStore(), StoreContext.home);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			startService(new Intent(this, PullingContentService.class));
			if (ManagerPreferences.isAutoUpdateEnable()) {
				final PermissionManager permissionManager = new PermissionManager();
				final DownloadServiceHelper downloadManager = new DownloadServiceHelper(AptoideDownloadManager.getInstance(), permissionManager);
				new AutoUpdate(this, new InstallManager(permissionManager, getPackageManager(), new DownloadInstallationProvider(downloadManager)),
						new DownloadFactory(), downloadManager).execute();
			}
			if(SecurePreferences.isFirstRun()){
				pushFragmentV4(new BaseWizardViewerFragment());
				SecurePreferences.setFirstRun(false);
			}

			// Deep Links
			if (getIntent().hasExtra(DeepLinksIds.NEW_REPO_EXTRA) && getIntent().getFlags() == DeepLinksIds.NEW_REPO_FLAG) {
				newrepoDeepLink(getIntent());
			} else if (getIntent().hasExtra(DeepLinksIds.FROM_DOWNLOAD_NOTIFICATION)) {
				downloadNotificationDeepLink(getIntent());
			} else if (getIntent().hasExtra(DeepLinksIds.FROM_TIMELINE)) {
				fromTimelineDeepLink(getIntent());
			} else if (getIntent().hasExtra(DeepLinksIds.NEW_UPDATES)) {
				newUpdatesDeepLink(getIntent());
			} else {
				// TODO: 10-08-2016 jdandrade mudei isto da AptoideBaseActivity para aqui, confirma se é o que pretendes.
				Analytics.ApplicationLaunch.launcher();
			}
		}
	}

	private void newrepoDeepLink(Intent intent) {
		ArrayList<String> repos = intent.getExtras().getStringArrayList(DeepLinksIds.NEW_REPO_EXTRA);
		if (repos != null) {

			for (final String repoUrl : repos) {

				String storeName = StoreUtils.split(repoUrl);
				if (StoreUtils.isSubscribedStore(storeName)) {
					ShowMessage.asToast(this, getString(R.string.store_already_added));
				} else {
					StoreUtilsProxy.subscribeStore(storeName);
					setMainPagerPosition(Event.Name.myStores);
				}
			}

			getIntent().removeExtra(DeepLinksIds.NEW_REPO_EXTRA);
		}
	}

	private void downloadNotificationDeepLink(Intent intent) {
		// TODO: 10-08-2016 jdandrade
		Analytics.ApplicationLaunch.downloadingUpdates();
		setMainPagerPosition(Event.Name.myStores);
	}

	private void fromTimelineDeepLink(Intent intent) {
		// TODO: 10-08-2016 jdandrade
		Analytics.ApplicationLaunch.timelineNotification();
		setMainPagerPosition(Event.Name.getUserTimeline);
	}

	private void newUpdatesDeepLink(Intent intent) {
		// TODO: 10-08-2016 jdandrade
		Analytics.ApplicationLaunch.newUpdatesNotification();
		setMainPagerPosition(Event.Name.myUpdates);
	}

	private void setMainPagerPosition(Event.Name name) {
		AptoideUtils.ThreadU.runOnIoThread(() -> {
			AptoideUtils.ThreadU.runOnUiThread(() -> {
				if (!(getCurrentFragment() instanceof HomeFragment)) {
					return;
				}

				((HomeFragment) getCurrentFragment()).setDesiredViewPagerItem(name);
			});
		});
	}

	@Override
	public void pushFragment(Fragment fragment) {
		FragmentUtils.replaceFragment(this, fragment);
	}

	@Override
	public void pushFragmentV4(android.support.v4.app.Fragment fragment) {
		FragmentUtils.replaceFragmentV4(this, fragment);
	}

	@Override
	public void popFragment() {
		onBackPressed();
	}

	public android.support.v4.app.Fragment getCurrentV4() {
		return FragmentUtils.getFirstFragmentV4(this);
	}

	public Fragment getCurrent() {
		return FragmentUtils.getFirstFragment(this);
	}

	public android.support.v4.app.Fragment getLastV4() {
		return FragmentUtils.getLastFragmentV4(this);
	}

	public Fragment getLast() {
		return FragmentUtils.getLastFragment(this);
	}

	@Override
	public void onBackPressed() {

		// A little hammered to close the drawer on back pressed :)
		if (getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1) instanceof DrawerFragment) {
			DrawerFragment fragment = (DrawerFragment) getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);
			if (fragment.isDrawerOpened()) {
				fragment.closeDrawer();
				return;
			} else {
				super.onBackPressed();
			}
		}

		super.onBackPressed();
	}

	public static class DeepLinksIds {

		public final static String NEW_REPO_EXTRA = "newrepo";
		public final static int NEW_REPO_FLAG = 12345;
		public static final String FROM_DOWNLOAD_NOTIFICATION = "fromDownloadNotification";
		public static final String FROM_TIMELINE = "fromTimeline";
		public static final String NEW_UPDATES = "new_updates";
	}
}
