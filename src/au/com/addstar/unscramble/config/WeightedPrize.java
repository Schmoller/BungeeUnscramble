package au.com.addstar.unscramble.config;

public class WeightedPrize
{
	public WeightedPrize(int weight, String prize)
	{
		this.weight = weight;
		this.prize = prize;
	}
	
	public WeightedPrize() {}
	
	public int weight;
	public String prize;
}
