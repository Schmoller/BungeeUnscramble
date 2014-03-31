package au.com.addstar.unscramble.prizes;

import java.util.Map;

public class Prizes
{
	public static Prize load(Map<String, Object> map)
	{
		String type = (String)map.get("type");
		
		if(type.equals("money"))
			return MoneyPrize.load(map);
		
		return null;
	}
}
