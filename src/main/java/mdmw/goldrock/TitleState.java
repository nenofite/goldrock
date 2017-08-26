package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.input.controls.ActionListener;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

/**
 * Display the title screen and "Click to start"
 */
public class TitleState extends AbstractAppState implements ActionListener
{
    private Main app;
    private Node node;
    private AudioNode music;

    public TitleState()
    {
        node = new Node("Title State");
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);
        this.app = (Main) app;

        // Add the title background
        node.attachChild(makeTitleBackground());

        // Register as a listener
        app.getInputManager().addListener(this, Main.NEXT_SCREEN_MAPPING);

        // Attach our node
        this.app.getGuiNode().attachChild(node);

        // Make the music node
        music = new AudioNode(app.getAssetManager(), "Audio/victory_music.wav", AudioData.DataType.Stream);
        music.setLooping(true);
        music.setPositional(false);
        node.attachChild(music);
        music.play();
    }

    @Override
    public void cleanup()
    {
        super.cleanup();

        // Deregister our listener
        app.getInputManager().removeListener(this);

        music.stop();

        // Detach our node
        node.removeFromParent();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        if (Main.NEXT_SCREEN_MAPPING.equals(name) && isPressed)
        {
            // Switch to shoot deer state
            app.getStateManager().detach(this);
            app.getStateManager().attach(new NewspaperState());
        }
    }


    /**
     * Make the title picture background, which displays the title of the game and the prompt "Click to start"
     *
     * @return A background image
     */
    private Spatial makeTitleBackground()
    {
        Picture bg = new Picture("Title Background");
        bg.setImage(app.getAssetManager(), "Sprites/title.png", true);
        bg.setWidth(app.getCamera().getWidth());
        bg.setHeight(app.getCamera().getHeight());

        return bg;
    }
}
