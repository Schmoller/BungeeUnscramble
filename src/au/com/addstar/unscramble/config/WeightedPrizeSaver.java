package au.com.addstar.unscramble.config;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.cubespace.Yamler.Config.InternalConverter;
import net.cubespace.Yamler.Config.Converter.Converter;

public class WeightedPrizeSaver implements Converter
{
	public WeightedPrizeSaver(InternalConverter converter)
	{
	}
	
	@Override
	public Object fromConfig( Class<?> clazz, Object obj, ParameterizedType type ) throws Exception
	{
		if((obj instanceof WeightedPrize))
		{
			return (WeightedPrize)obj;
		}

		if(!(obj instanceof Map))
			return null;

		Map<?, ?> map = (Map<?, ?>)obj;

		if(map.isEmpty())
			return null;

		WeightedPrize prize = new WeightedPrize();
		Entry<?, ?> entry = map.entrySet().iterator().next();
		prize.weight = Integer.valueOf((String)entry.getKey());
		prize.prize = (String)entry.getValue();
		
		return prize;
	}

	@Override
	public boolean supports( Class<?> clazz )
	{
		return clazz.equals(WeightedPrize.class);
	}

	@Override
	public Object toConfig( Class<?> clazz, Object obj, ParameterizedType type ) throws Exception
	{
		WeightedPrize prize = (WeightedPrize)obj;
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(String.valueOf(prize.weight), prize.prize);
		
		return map;
	}

}
