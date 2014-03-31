package au.com.addstar.unscramble.prizes;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

import net.cubespace.Yamler.Config.InternalConverter;
import net.cubespace.Yamler.Config.Converter.Converter;

public class PrizeSaver implements Converter
{
	public PrizeSaver(InternalConverter converter)
	{
	}
	
	@Override
	public Object fromConfig( Class<?> clazz, Object obj, ParameterizedType type ) throws Exception
	{
		if(!(obj instanceof Map))
			return null;
		
		@SuppressWarnings( "unchecked" )
		Map<String, Object> map = (Map<String, Object>)obj;
		
		return Prizes.load(map);
	}

	@Override
	public boolean supports( Class<?> clazz )
	{
		return Prize.class.isAssignableFrom(clazz);
	}

	@Override
	public Object toConfig( Class<?> clazz, Object obj, ParameterizedType type ) throws Exception
	{
		if(!(obj instanceof Prize))
			return null;
		
		Prize prize = (Prize)obj;
		
		return prize.save();
	}

}
