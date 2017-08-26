package mdmw.goldrock;

import org.junit.Assert;
import org.junit.Test;

public class AnimationStationTest
{
    @Test
    public void singleLoopTest()
    {
        String firstName = "first";
        String secondName = "second";
        String thirdName = "third";
        AnimationStation stat = new AnimationStation();
        Assert.assertEquals(0.0f, stat.getAnimationDuration(), 0.01f);
        stat.addImage(firstName, 0.5f);
        Assert.assertEquals(0.5f, stat.getAnimationDuration(), 0.01f);
        Assert.assertEquals(stat.getCurrent(), firstName);
        stat.addImage(secondName, 1.0f);
        stat.addImage(thirdName, 1.5f);
        stat.progress(0.75f);
        Assert.assertEquals(secondName, stat.getCurrent());
        stat.progress(1.0f);
        Assert.assertEquals(thirdName, stat.getCurrent());
        stat.progress(stat.getAnimationDuration());
        Assert.assertEquals(thirdName, stat.getCurrent());
        stat.progress(stat.getAnimationDuration() * 3 + 1.5f);
        Assert.assertEquals(firstName, stat.getCurrent());
    }

    @Test(expected = IllegalStateException.class)
    public void illegalProgressTest()
    {
        AnimationStation stat = new AnimationStation();
        stat.progress(1.0f);
    }

    @Test
    public void animationDuplicateTest()
    {
        String firstName = "first";
        String secondName = "second";
        String thirdName = "third";
        AnimationStation stat = new AnimationStation();
        stat.addImage(firstName, 0.5f);
        stat.addImage(secondName, 1.0f);
        stat.addImage(thirdName, 1.5f);
        AnimationStation stat2 = new AnimationStation(stat);
        Assert.assertEquals(3.0f, stat2.getAnimationDuration(), 0.1f);
        Assert.assertEquals(firstName, stat.getCurrent());
        stat.progress(0.25f);
        Assert.assertEquals(firstName, stat.getCurrent());
        stat.progress(0.5f);
        Assert.assertEquals(secondName, stat.getCurrent());
        stat.progress(1f);
        Assert.assertEquals(thirdName, stat.getCurrent());
    }
}
