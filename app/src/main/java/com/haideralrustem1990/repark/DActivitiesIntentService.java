package com.haideralrustem1990.repark;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;


public class DActivitiesIntentService extends IntentService {

    final String TAG = DActivitiesIntentService.class.getSimpleName();
    final int code = 1;
    int notificationID = 0;
    String text = "";


    ArrayList<DActivity> list = new ArrayList<>();
    ArrayList<String> stringList = new ArrayList<>(); // this is used for debugging (by broadcasting it)

    DActivity tilting = null;
    DActivity walking = null;
    DActivity running = null;
    DActivity still = null;
    DActivity foot = null;
    DActivity vehicle = null;
    DActivity unknown = null;
    DActivity bicycle = null;


    final int[] POSSIBLE_ACTIVITIES = {

            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.TILTING,
            DetectedActivity.UNKNOWN
    };

    final int[] transitionTypes = {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER,
            ActivityTransition.ACTIVITY_TRANSITION_EXIT
    };


    public DActivitiesIntentService() {
        super("DActivitiesIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Check whether the Intent contains activity recognition data//
        if (ActivityRecognitionResult.hasResult(intent)) {


            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            for (DetectedActivity activity : detectedActivities) {

                //stringList.add(getActivityString(activity.getType()) + " = " +
                       // String.valueOf(activity.getConfidence()));

                if (getActivityString(activity.getType()).equalsIgnoreCase("tilting")) {
                    tilting = new DActivity(activity.getConfidence(), "tilting");
                    Log.v("HAIDER", tilting.name + " " + String.valueOf(tilting.confidence));
                    list.add(tilting);
                } else if (getActivityString(activity.getType()).equalsIgnoreCase("walking")) {
                    walking = new DActivity(activity.getConfidence(), "walking");
                    Log.v("HAIDER", walking.name + " " + String.valueOf(walking.confidence));
                    list.add(walking);
                } else if (getActivityString(activity.getType()).equalsIgnoreCase("still")) {
                    still = new DActivity(activity.getConfidence(), "still");
                    Log.v("HAIDER", still.name + " " + String.valueOf(still.confidence));
                    list.add(still);
                } else if (getActivityString(activity.getType()).equalsIgnoreCase("running")) {
                    running = new DActivity(activity.getConfidence(), "running");
                    Log.v("HAIDER", running.name + " " + String.valueOf(running.confidence));
                    list.add(running);
                } else if (getActivityString(activity.getType()).equalsIgnoreCase("vehicle")) {
                    vehicle = new DActivity(activity.getConfidence(), "vehicle");
                    Log.v("HAIDER", vehicle.name + " " + String.valueOf(vehicle.confidence));
                    list.add(vehicle);
                } else if (getActivityString(activity.getType()).equalsIgnoreCase("foot")) {
                    foot = new DActivity(activity.getConfidence(), "foot");
                    Log.v("HAIDER", foot.name + " " + String.valueOf(foot.confidence));
                    list.add(foot);
                } else if (getActivityString(activity.getType()).equalsIgnoreCase("unknown_activity")) {
                    unknown = new DActivity(activity.getConfidence(), "unknown_activity");
                    Log.v("HAIDER", unknown.name + " " + String.valueOf(unknown.confidence));
                    list.add(unknown);
                } else if (getActivityString(activity.getType()).equalsIgnoreCase("bicycle")) {
                    bicycle = new DActivity(activity.getConfidence(), "bicycle");
                    Log.v("HAIDER", bicycle.name + " " + String.valueOf(bicycle.confidence));
                    list.add(bicycle);
                }

            }

            // check to see if user started driving (when vehicle confidence goes above )
            if (vehicle != null) {
                if (vehicle.confidence >= 31 && MainActivity.startedDriving == false) {

                    MainActivity.startedDriving = true; // record that user has driven
                    MainActivity.finishedDriving = false;

                }
            }
            else if((vehicle == null || vehicle.confidence <= 30))

            {   // user stopped driving

                if((walking != null && walking.confidence >=25) ||
                        (foot != null && foot.confidence >= 25)  ||
                        (running != null && running.confidence >= 25) ) {

                        if (MainActivity.startedDriving == true) {

                        issueNotification("Do you want to remember your parking location?");
                        MainActivity.startedDriving = false;
                        MainActivity.finishedDriving = true;
                    }
                }
            }

            for(DActivity dac: list){
                Log.v("HAIDER", dac.name + " " + String.valueOf(dac.confidence));
            }

            // this method sends two data: stringList and vars to any broadcastReceiver that is
            // programmed to listen to it (using action string labels)

            // broadcastActivity(stringList , vars);
        }

    }

    public void issueNotification(String text){
        // context is the Service/Activity that will initiate this notification.
        // pendingIntent  is the intent that specifies what Activity should run when
        // the notification is clicked

        Intent resultIntent = new Intent(this, MainActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
// Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                "com.haideralrustem1990.someapp.Notifications")
                .setSmallIcon(R.drawable.mainicon)   // icon for notification
                .setContentTitle("Parking Reminder")
                .setContentText(text)
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // the priority for the notification
                .setVibrate(new long[] {0, 1500})
                .setAutoCancel(true);
        NotificationManager nm =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        nm.notify(notificationID++, builder.build());


    }

    static String getActivityString(int detectedActivityType) {

        switch(detectedActivityType) {
            case DetectedActivity.ON_BICYCLE:
                return "bicycle";
            case DetectedActivity.ON_FOOT:
                return "foot";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.IN_VEHICLE:
                return "vehicle";
            default:
                return "unknown_activity";
        }
    }

    // This a method that were used for debugging. It sends an intent with label "activities_intent" to all receivers
    private void broadcastActivity(ArrayList<String> strArrayList, String vars) {
        // The "activity_intent" is the label that BroadcastReceivers use to identify and respond to this intent
        Intent intent = new Intent("activities_intent");
        intent.putStringArrayListExtra("activity", strArrayList);
        intent.putExtra("variables", vars);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);  // broadcast the intent
    }

}
