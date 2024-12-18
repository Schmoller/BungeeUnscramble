package au.com.addstar.unscramble.prizes;

import java.util.Map;

public class Prizes
{
	public static Prize load(Map<String, Object> map)
	{
		String type = (String)map.get("type");
		
		if(type.equals("money"))
			return MoneyPrize.load(map);
		else if(type.equals("item"))
			return ItemPrize.load(map);

		return null;
	}
	
	public static Prize parse(String prize)
	{
		if(prize.startsWith("$"))
		{
			try
			{
				double amount = Double.parseDouble(prize.substring(1));
				if(amount < 0)
					throw new IllegalArgumentException("Cannot give negative money");
				
				return new MoneyPrize(amount);
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("Expected $<amount> for money prize");
			}
		}
		else if(prize.startsWith("item "))
		{
			String[] parts = prize.split(" ");
			if(parts.length != 3 && parts.length != 4)
				throw new IllegalArgumentException("Expected 'item <material> [data] <count>'");
			
			String material = parts[1];
			int data = 0;
			int count = 1;
			
			if(parts.length == 4)
			{
				try
				{
					data = Integer.parseInt(parts[2]);
					if(data < 0 || data > 15)
						throw new IllegalArgumentException("Data value must be in the range of 0-15 inclusive");
				}
				catch(NumberFormatException e)
				{
					throw new IllegalArgumentException("Data value must be a number in the range of 0-15 inclusive");
				}
			}
			
			try
			{
				count = Integer.parseInt(parts[parts.length-1]);
				if(count < 0 || count > 64)
					throw new IllegalArgumentException("Count must be in the range of 0-64 inclusive");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("Count must be a number in the range of 0-64 inclusive");
			}
			
			return new ItemPrize(material, data, count);
		}
		else if(prize.startsWith("points"))
		{
			return new PointsPrize();
		}

		return null;
	}
}
