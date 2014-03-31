package au.com.addstar.unscramble;

public class AutoGameStarter implements Runnable
{
	@Override
	public void run()
	{
		Unscramble.instance.startAutoGame();
	}
}
