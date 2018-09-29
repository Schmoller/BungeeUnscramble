package au.com.addstar.unscramble.config;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 29/09/2018.
 */
public class GameConfigTest {

    @Test
    public void initializeTest() {
        File resourcesDirectory = new File("src/test/resources/");
        File main = new File(resourcesDirectory, "config.yml");
        File unclaimed = new File(resourcesDirectory, "unclaimed.yml");
        File auto = new File(resourcesDirectory, "auto.yml");
        MainConfig config = new MainConfig(main);
        try {
            config.init();
            List<String> words = config.words;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        GameConfig gConfig = new GameConfig();
        try {
            gConfig.init(auto);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }


    }
}
