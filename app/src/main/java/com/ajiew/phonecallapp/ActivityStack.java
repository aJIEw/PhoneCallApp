package com.ajiew.phonecallapp;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ActivityStack {

    private static final ActivityStack INSTANCE = new ActivityStack();

    private final List<Activity> activities = new ArrayList<>();

    public static ActivityStack getInstance() {
        return INSTANCE;
    }

    public void addActivity(Activity activity) {
        if (activity != null) {
            activities.add(activity);
        }
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
        if (activityClass == null) {
            return;
        }

        Iterator<Activity> iterator = activities.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            if (activity != null && activity.getClass().equals(activityClass)) {
                iterator.remove();
                activity.finish();
            }
        }
    }

    public void finishAllActivity() {
        Iterator<Activity> iterator = activities.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            iterator.remove();
            if (activity != null) {
                activity.finish();
            }
        }
    }
}
