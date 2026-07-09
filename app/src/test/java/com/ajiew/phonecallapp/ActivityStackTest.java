package com.ajiew.phonecallapp;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;

import org.junit.After;
import org.junit.Test;

public class ActivityStackTest {

    @After
    public void tearDown() {
        ActivityStack.getInstance().finishAllActivity();
    }

    @Test
    public void finishActivityByClass_removesMatchingActivitiesWithoutCrash() {
        TestActivity firstActivity = new TestActivity();
        TestActivity secondActivity = new TestActivity();

        ActivityStack.getInstance().addActivity(firstActivity);
        ActivityStack.getInstance().addActivity(secondActivity);

        ActivityStack.getInstance().finishActivity(TestActivity.class);

        assertTrue(firstActivity.finished);
        assertTrue(secondActivity.finished);
        assertNull(ActivityStack.getInstance().getTopActivity());
    }

    public static class TestActivity extends Activity {
        boolean finished;

        @Override
        public void finish() {
            finished = true;
        }
    }
}
