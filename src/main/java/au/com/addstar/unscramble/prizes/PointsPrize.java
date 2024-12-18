package au.com.addstar.unscramble.prizes;

import au.com.addstar.unscramble.MessageOutput;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PointsPrize extends Prize
{
	int mPoints = 0;
	int mDifficulty = 0;

	public PointsPrize()
	{
	}

	public void setDifficulty(int mDifficulty) {
		this.mDifficulty = mDifficulty;
	}

	public void setPoints(int points)
	{
		mPoints = points;
	}

	@Override
	public String getDescription()
	{
		return mPoints + " POINT" + (mPoints > 1 ? "S" : "");
	}

	@Override
	public int award(ProxiedPlayer player) {
		return mPoints;
	}

	@Override
	public Entry<Prize, String> handleFail(DataInputStream input) throws IOException {
		return null;
	}

	@Override
	public Map<String, Object> save() {
		return null;
	}

	public static PointsPrize load(Map<String, Object> map)
	{
		return new PointsPrize();
	}
}
