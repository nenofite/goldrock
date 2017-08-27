package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.input.controls.ActionListener;
import com.jme3.scene.Node;

/**
 * Displays a newspaper with plot progression. The newspaper is a picture that flies up to the screen. The player
 * clicks once they are done reading.
 */
public class NewspaperState extends AbstractAppState implements ActionListener
{
    private Main app;
    private Node node;
    private final int killCount;
    private AudioNode music;
    private int intermissionNumber;

    public NewspaperState(int killCount, int intermissionNumber)
    {
        node = new Node("Newspaper State");
        this.killCount = killCount;
        this.intermissionNumber = intermissionNumber;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);
        this.app = (Main) app;
        this.app.getGuiNode().attachChild(node);

        // Make the newspaper
        node.attachChild(NewspaperControl.makeNewspaper(getNewspaper(killCount, intermissionNumber), this.app));

        // Register as an event listener
        app.getInputManager().addListener(this, Main.NEXT_SCREEN_MAPPING);

        music = new AudioNode(app.getAssetManager(), "Audio/rag_neutral.wav", AudioData.DataType.Stream);
        music.setLooping(true);
        music.setPositional(false);
        node.attachChild(music);
        music.play();
    }

    @Override
    public void cleanup()
    {
        super.cleanup();
        node.removeFromParent();

        music.stop();

        // Remove ourselves as a listener
        app.getInputManager().removeListener(this);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        if (Main.NEXT_SCREEN_MAPPING.equals(name) && isPressed)
        {
            // Move on to the next screen
            app.getStateManager().detach(this);
            app.getStateManager().attach(new ShootDeerState(intermissionNumber + 1, killCount));
        }
    }

    /**
     * Get the path of the newspaper image based on the given kill count
     *
     * @param killCount          The number of deers killed
     * @param intermissionNumber How many hunts we've had
     * @return The path to an image
     */
    private String getNewspaper(int killCount, int intermissionNumber)
    {
        switch (intermissionNumber)
        {
            case 1:
                if (killCount < 10)
                {
                    return "Sprites/newspaper_1.png";
                } else
                {
                    return "Sprites/newspaper_2.png";
                }
            case 2:
                return "Sprites/newspaper_1.png";
            case 3:
                return "Sprites/newspaper_1.png";
            default:
                throw new IllegalStateException("We only have three intermissions!");
        }
    }
}
