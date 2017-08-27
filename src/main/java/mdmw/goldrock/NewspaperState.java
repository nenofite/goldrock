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
    private final float killFrac;
    private Main app;
    private Node node;
    private AudioNode music;

    public NewspaperState(float killFrac)
    {
        node = new Node("Newspaper State");
        this.killFrac = killFrac;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);
        this.app = (Main) app;
        this.app.getGuiNode().attachChild(node);

        // Make the newspaper
        node.attachChild(NewspaperControl.makeNewspaper(getNewspaper(killFrac), this.app));

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

        music.stop();

        // Remove ourselves as a listener
        app.getInputManager().removeListener(this);

        Utils.detachAllControls(node);
        node.removeFromParent();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        if (Main.NEXT_SCREEN_MAPPING.equals(name) && isPressed)
        {
            // Move on to the next screen
            app.getStateManager().detach(this);
            app.getStateManager().attach(new PlayAgainState());
        }
    }

    /**
     * Get the path of the newspaper image based on the given kill count
     *
     * @param killFrac The fraction of all deer that were killed
     * @return The path to an image
     */
    private String getNewspaper(float killFrac)
    {
        if (killFrac == 0)
        {
            return "Sprites/newspaper_zero.png";

        } else if (killFrac < 0.4f)
        {
            return "Sprites/newspaper_low.png";

        } else if (killFrac < 0.6f)
        {
            return "Sprites/newspaper_medium.png";

        } else if (killFrac < 0.8f)
        {
            return "Sprites/newspaper_high.png";

        } else if (killFrac < 1)
        {
            return "Sprites/newspaper_veryhigh.png";
        } else
        {
            return "Sprites/newspaper_all.png";
        }
    }
}
