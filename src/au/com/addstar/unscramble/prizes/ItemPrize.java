package au.com.addstar.unscramble.prizes;

import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ItemPrize extends Prize
{
	private String mMaterial;
	private int mData;
	private int mCount;
	
	public ItemPrize(String material, int data, int count)
	{
		mMaterial = material;
		mData = data;
		mCount = count;
	}
	
	
	@Override
	public String getDescription()
	{
		return mCount + " " + mMaterial + (mCount > 1 ? "s" : "");
	}

	@Override
	public void award( ProxiedPlayer player )
	{
	}

	@Override
	public Map<String, Object> save()
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("type", "item");
		map.put("material", mMaterial);
		map.put("data", mData);
		map.put("count", mCount);
		
		return map;
	}

	public static ItemPrize load(Map<String, Object> map)
	{
		return new ItemPrize((String)map.get("material"), (Integer)map.get("data"), (Integer)map.get("count"));
	}
}
