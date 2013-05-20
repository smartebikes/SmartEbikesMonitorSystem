package artsmedia.ebikes;

import artsmedia.ebikes.EBikesDataListener;

interface EBikesDataAPI {
	float getUpdate();
	boolean isBikeMoving();
	void addListener(EBikesDataListener listener);
	void removeListener(EBikesDataListener listener);
}