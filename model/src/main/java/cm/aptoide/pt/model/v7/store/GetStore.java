/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 26/04/2016.
 */

package cm.aptoide.pt.model.v7.store;

import cm.aptoide.pt.model.v7.BaseV7Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TODO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetStore extends BaseV7Response {

	private Nodes nodes;

	@Data
	public static class Nodes {

		private GetStoreMeta meta;
		private GetStoreTabs tabs;
		private GetStoreWidgets widgets;
	}
}
