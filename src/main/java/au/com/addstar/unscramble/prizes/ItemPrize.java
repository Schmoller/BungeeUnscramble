package au.com.addstar.unscramble.prizes;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import au.com.addstar.unscramble.MessageOutput;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ItemPrize extends Prize
{
	private final String mMaterial;
	private final int mData;
	private final int mCount;
	
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
	public int award( ProxiedPlayer player )
	{
		int session = nextSession++;
		new MessageOutput("bungee:unscramble", "AwardItem")
			.writeInt(session)
			.writeUTF(player.getName())
			.writeUTF(mMaterial)
			.writeByte(mData)
			.writeByte(mCount)
			.send(player.getServer().getInfo());
		
		return session;
	}
	
	@Override
	public Entry<Prize, String> handleFail( DataInputStream input ) throws IOException
	{
		int remaining = input.readInt();
		return new AbstractMap.SimpleEntry<>(new ItemPrize(mMaterial, mData, remaining), "Your inventory was full. Please clear space then try again");
	}

	@Override
	public Map<String, Object> save()
	{
		HashMap<String, Object> map = new HashMap<>();
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
