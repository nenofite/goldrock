package mdmw.goldrock;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.system.AppSettings;

public class Main extends SimpleApplication {

    public static void main(String[] args) {
        new Main().start();
    }

    public Main() {
        AppSettings settings = new AppSettings(true);
        settings.setVSync(true);
        settings.setFrameRate(100);
        setSettings(settings);
    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        flyCam.setEnabled(false);

        TitleState titleState = new TitleState();
        stateManager.attach(titleState);
    }

    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
    }


    public BitmapFont getGuiFont()
    {
        return guiFont;
    }
}
