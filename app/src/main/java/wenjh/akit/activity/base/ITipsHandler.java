package wenjh.akit.activity.base;


public interface ITipsHandler {
	public void addTips(TipsMessage message);
	public boolean isToptipViewShown();
	public void hideToptipDelayed(long time);
	public void removeTips(TipsMessage message);
	public void removeTips(int id);
}
