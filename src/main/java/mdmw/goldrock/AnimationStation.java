package mdmw.goldrock;

import com.jme3.asset.AssetManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class which tells a sprite which image it should display. Does not actually update the image. Also
 * requires frequent updates from the sprite itself.
 */
public class AnimationStation
{
    private List<AnimationComponent> components;
    private float currentTick;
    private int currentIndex;
    private float totalDuration;

    /**
     * Creates a basic, empty AnimationStation.
     */
    public AnimationStation()
    {
        components = new ArrayList<>();
        currentIndex = 0;
        currentTick = 0.0f;
        totalDuration = 0.0f;
    }

    /**
     * Creates a duplicate of the given AnimationStation, starting from the beginning of the given animation.
     *
     * @param base
     */
    public AnimationStation(AnimationStation base)
    {
        currentIndex = 0;
        currentTick = 0.0f;
        components = new ArrayList<>(base.components);
        totalDuration = base.totalDuration;
    }

    /**
     * Gets the path for the current image to be displayed by the tracked animation.
     *
     * @return
     */
    public String getCurrent()
    {
        return components.get(currentIndex).getImagePath();
    }

    public void progress(float tpf)
    {
        if (totalDuration <= 0.0f)
        {
            throw new IllegalStateException("The current total duration is 0. Cannot progress the animation.");
        }

        currentTick += tpf;
        while (currentTick > totalDuration)
        {
            currentTick -= totalDuration;
            currentIndex = 0;
        }
        while (components.get(currentIndex).getEndTime() < currentTick)
        {
            ++currentIndex;
        }
    }

    public void addImage(String pathName, float duration)
    {
        components.add(new AnimationComponent(pathName, duration, totalDuration));
        totalDuration += duration;
    }

    public float getAnimationDuration()
    {
        return totalDuration;
    }

    private class AnimationComponent
    {
        private String animationImage;
        private float animationDuration;
        private float endTime;

        public AnimationComponent(String imgPath, float duration, float previousDuration)
        {
            animationImage = imgPath;
            animationDuration = duration;
            endTime = previousDuration + duration;
        }

        public String getImagePath()
        {
            return animationImage;
        }

        public float getDuration()
        {
            return animationDuration;
        }

        public float getEndTime()
        {
            return endTime;
        }
    }
}
