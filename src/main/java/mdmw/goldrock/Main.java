package mdmw.goldrock;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.system.AppSettings;

public class Main extends SimpleApplication
{
    public static final String NEXT_SCREEN_MAPPING = "Next Screen";

    public static void main(String[] args)
    {
        new Main().start();
    }

    public Main()
    {
        AppSettings settings = new AppSettings(true);
        settings.setSettingsDialogImage("Sprites/title.png");
        settings.setVSync(true);
        settings.setFrameRate(100);
        setSettings(settings);
    }

    @Override
    public void simpleInitApp()
    {
        setDisplayStatView(false);
        flyCam.setEnabled(false);

        // Setup mappings

        // Map click or spacebar to start or go to next screen (eg. after newspaper)
        getInputManager().addMapping(NEXT_SCREEN_MAPPING, new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));

        // Register the mouse button and the space bar to shoot
        getInputManager().addMapping(ShootDeerState.SHOOT_MAPPING, new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));

        stateManager.attach(new TitleState());
    }

    @Override
    public void simpleUpdate(float tpf)
    {
        super.simpleUpdate(tpf);
    }

    public BitmapFont getGuiFont()
    {
        return guiFont;
    }
}
