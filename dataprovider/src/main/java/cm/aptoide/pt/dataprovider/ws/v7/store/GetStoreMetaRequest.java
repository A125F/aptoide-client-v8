/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 27/04/2016.
 */

package cm.aptoide.pt.dataprovider.ws.v7.store;

import cm.aptoide.pt.dataprovider.ws.v7.BaseBody;
import cm.aptoide.pt.dataprovider.ws.v7.V7;
import cm.aptoide.pt.model.v7.store.GetStoreMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import rx.Observable;

/**
 * Created by neuro on 19-04-2016.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetStoreMetaRequest extends V7<GetStoreMeta> {

	private final Body body = new Body();

	private GetStoreMetaRequest() {
	}

	public static GetStoreMetaRequest of(String storeName) {
		GetStoreMetaRequest getStoreRequest = new GetStoreMetaRequest();

		getStoreRequest.body.setStoreName(storeName);

		return getStoreRequest;
	}

	public static GetStoreMetaRequest of(int storeId) {
		GetStoreMetaRequest getStoreRequest = new GetStoreMetaRequest();

		getStoreRequest.body.setStoreId(storeId);

		return getStoreRequest;
	}

	@Override
	protected Observable<GetStoreMeta> loadDataFromNetwork(Interfaces interfaces) {
		return interfaces.getStoreMeta(body);
	}

	@Data
	@Accessors(chain = true)
	@EqualsAndHashCode(callSuper = true)
	public static class Body extends BaseBody {

		private Integer storeId;
		private String storeName;
		private String storePassSha1;
		private String storeUser;
	}
}
