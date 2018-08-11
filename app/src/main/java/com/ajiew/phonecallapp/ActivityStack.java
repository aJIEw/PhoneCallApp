package com.ajiew.phonecallapp;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * author: aJIEw
 * description: Activity 栈的管理
 */
public class ActivityStack {

    private static final ActivityStack INSTANCE = new ActivityStack();

    private List<Activity> activities = new ArrayList<>();

    public static ActivityStack getInstance() {
        return INSTANCE;
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public Activity getTopActivity() {
        if (activities.isEmpty()) {
            return null;
        }
        return activities.get(activities.size() - 1);
    }

    public void finishTopActivity() {
        if (!activities.isEmpty()) {
            activities.remove(activities.size() - 1).finish();
        }
    }

    public void finishActivity(Activity activity) {
        if (activity != null) {
            activities.remove(activity);
            activity.finish();
        }
    }

    public void finishActivity(Class activityClass) {
        for (Activity activity : activities) {
            if (activity.getClass().equals(activityClass)) {
                finishActivity(activity);
            }
        }
    }

    public void finishAllActivity() {
        if (!activities.isEmpty()) {
            for (Activity activity : activities) {
                activity.finish();
                activities.remove(activity);
            }
        }
    }
}
