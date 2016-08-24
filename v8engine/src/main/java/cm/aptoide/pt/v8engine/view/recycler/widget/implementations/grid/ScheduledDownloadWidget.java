/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 21/07/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.widget.implementations.grid;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import cm.aptoide.pt.database.realm.Scheduled;
import cm.aptoide.pt.imageloader.ImageLoader;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.ScheduledDownloadDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.widget.Displayables;
import cm.aptoide.pt.v8engine.view.recycler.widget.Widget;

/**
 * created by SithEngineer
 */
@Displayables({ScheduledDownloadDisplayable.class})
public class ScheduledDownloadWidget extends Widget<ScheduledDownloadDisplayable> {
	
	private ImageView appIcon;
	private TextView appName;
	private TextView appVersion;
	private CheckBox isSelected;

	public ScheduledDownloadWidget(View itemView) {
		super(itemView);
	}

	@Override
	protected void assignViews(View itemView) {
		appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
		appName = (TextView) itemView.findViewById(R.id.app_name);
		appVersion = (TextView) itemView.findViewById(R.id.app_version);
		isSelected = (CheckBox) itemView.findViewById(R.id.is_selected);
	}

	@Override
	public void bindView(ScheduledDownloadDisplayable displayable) {
		Scheduled scheduled = displayable.getPojo();
		ImageLoader.load(scheduled.getIcon(), appIcon);
		appName.setText(scheduled.getName());
		appVersion.setText(scheduled.getVersionName());

		isSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
			displayable.setSelected(isChecked);
		});

		isSelected.setChecked(displayable.isSelected());
	}

	@Override
	public void onViewAttached() {

	}

	@Override
	public void onViewDetached() {

	}
}
