package au.com.addstar.unscramble.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.Path;

public class MainConfig extends Config
{
	public MainConfig(File file)
	{
		CONFIG_FILE = file;
	}
	
	@Path("random-words")
	public List<String> words = new ArrayList<String>();
	
	@Path("display-answer-on-failed-games")
	public boolean displayAnswer = true;
	
	@Path("auto-game-enabled")
	public boolean autoGameEnabled = false;
	
	@Path("claim-servers")
	@Comment("The names of servers players may claim their rewards on")
	public List<String> claimServers = new ArrayList<String>();
}
