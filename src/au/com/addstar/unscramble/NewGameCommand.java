package au.com.addstar.unscramble;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class NewGameCommand extends Command
{
	public NewGameCommand()
	{
		super("newgame");
	}
	
	@Override
	public void execute( CommandSender sender, String[] args )
	{
		Session session = new Session("test word", 30000);
		session.start();
	}

}
