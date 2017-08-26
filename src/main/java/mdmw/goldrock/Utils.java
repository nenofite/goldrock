package mdmw.goldrock;

import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

public final class Utils
{
    private Utils()
    {
    }


    /**
     * Check if any parent of this spatial has the given control and return if it does. Otherwise return null.
     * This can be used for extracting DeerControls from deer picture geometries, for example.
     *
     * @param spatial      The spatial to check
     * @param controlClass The type of control to look for
     * @param <T>          The type of control to look for
     * @return The control class, or null
     */
    public static <T extends Control> T extractControl(Spatial spatial, Class<T> controlClass)
    {
        Spatial it = spatial;
        while (it != null)
        {
            T result = it.getControl(controlClass);
            if (result != null)
            {
                return result;
            }

            it = it.getParent();
        }

        return null;
    }
}
