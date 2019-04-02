package com.haideralrustem1990.repark;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Connector {
    private static int initialCapacity = 3;
    private static int fragmentCount = 0;
    String saveFileName = "SaveFileRepark.txt";
    ContentValues values;  // This will be used to get around passing Extra data with the camera intent

    private Uri currentImageUri;  // This var will record the image uri that is currently being captured
    // (this var is not used in the occurrences array, though its value may be equal at some
    // point to the value that was stored in the occurrence object)
    private Bitmap currentBitmapImage;
    private static final int CAMERA_REQUEST = 1888;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 112;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 113;
    public static final int MY_ACTIVITY_RECOGNITION_PERMISSION_CODE = 114;  // some random identifier



    public static ArrayList<Occurrence> occurrences = new ArrayList<>(initialCapacity);

    static String TAG = "- - - DEBUG - - -  ";
    boolean dialogAppeared =false;

    //---- Variables For Activity Recognition functionality -------------------------------

    static long DETECTION_INTERVAL_IN_MILLISECONDS = 1000;

    // An arraylist that will be used to capture results from the IntentService broadcaster
    static ArrayList <String> detectedActivitiesList = new ArrayList<>();

    private ActivityRecognitionClient mActivityRecognitionClient;   // the client that will initiate the IntentService when he has got activity updates
    BroadcastReceiver broadcastReceiver;
    String text = "variables";

    static boolean startedDriving = false;  // these are used to track user driving to trigger right actions
    static boolean finishedDriving = true;

    //------------------------------------------------------------------------------------


    public void loadArrayFromFile(){ // <<<=================  load data from file
        FileHandler fh = new FileHandler();
        boolean fileExists = fh.checkIfFileExists(saveFileName, this);
        if(fileExists){
             occurrences = fh.readFromFile(saveFileName, this);
             Log.d(TAG, occurrences.toString());
            Log.d(TAG, "FILE FOUND !!");
        }
        else{
            Log.d(TAG, occurrences.toString());
            Log.d(TAG, "FILE WAS NOT FOUND !!");
        }

    }

    public static Occurrence peek(){
        return occurrences.get(occurrences.size() - 1);
    }
    public static Occurrence peek(int pos) {
        return occurrences.get(pos);
    }

    public static Occurrence dequeueOccurrence(){
        /* Method needed to dequeue first item when user adds new occurrence even with
         * the array of occurrences having full capacity (we want to make room so we kick
         * the first)*/
        Occurrence removed = occurrences.remove(0);

        Log.d(TAG, "dequeued occurrences. occurrences = " + occurrences.toString());
        return removed;
    }

    public static Occurrence pushOccurence(Occurrence occurrence){
        Occurrence pushedAwayOccurence = new Occurrence();

        if(occurrences.size() >= initialCapacity){
            Log.d("MainActivity", String.valueOf(occurrences.size()));
            Log.d("Dequeue", "array is full, so we dequeue");
            pushedAwayOccurence = dequeueOccurrence();
            occurrences.add(occurrence);
            Log.d("TAG", "supposedly added. Array is "+ occurrences.toString());

        }
        else {
            occurrences.add(occurrence);
            pushedAwayOccurence = null;
            Log.d("TAG", "supposedly added. Array is "+ occurrences.toString());
        }
        return pushedAwayOccurence;
    }
    public static Occurrence popOccurence(){
        Occurrence removedItem = null;
        if(occurrences.size() > 0){
            removedItem = occurrences.remove(occurrences.size() - 1);
        }
        else {
            Log.d("MainActivity", "array is empty, cannot pop");

        }
        return removedItem;
    }
    public static Occurrence removeOccurence(int index){
        Occurrence removedItem = null;
        if(occurrences.size() > 0){
            removedItem = occurrences.remove(index);
        }
        else {
            Log.d(" ---->", "array is empty, cannot Remove");

        }
        return removedItem;
    }


    public void checkingIfSavedFileExists(){
        /* A simple method to greet the user letting them know if they have saved files*/
        FileHandler fh = new FileHandler();
        if (fh.checkIfFileExists(saveFileName, this)){

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Needed", Toast.LENGTH_SHORT).show();
            }
             else{
                 Toast.makeText(this, "Loading your saved locations", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Preparing", Toast.LENGTH_SHORT).show();
        }
    }

    //---------- Parent methods-----------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.custom_bar);
        // this below was only way to change color of title for toolbar
        toolbar.setTitle(Html.fromHtml("<font color='#ffffff'>RE-PARK </font>"));
        Resources res = getResources();
        Drawable d = res.getDrawable(R.drawable.menuoptions40px);
        toolbar.setOverflowIcon(d);
        setSupportActionBar(toolbar);


        // Onboarding tutorial
        Intent introIntent = new Intent(MainActivity.this, OnboardingActivity.class);
        FileHandler fh = new FileHandler();
        if(savedInstanceState == null){
            if (!fh.checkIfFileExists(saveFileName, this)){
                startActivity(introIntent);
            }
        }

        // ---- checking read permission ---
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                requestReadPermission();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }



        if(savedInstanceState == null){
            checkingIfSavedFileExists();
            loadArrayFromFile(); // The method will create a filehandler object which will read data
            // from the savedFileName
        }

        fragmentCount = occurrences.size();
        SectionsPagerAdapter theAdapter = new SectionsPagerAdapter(
          getSupportFragmentManager(), fragmentCount);

        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(theAdapter);  // view is rendered

        FloatingActionButton saveFab = (FloatingActionButton) findViewById(R.id.save_button);
        FloatingActionButton removeFab = (FloatingActionButton) findViewById(R.id.remove_button);
        FloatingActionButton cameraFab = (FloatingActionButton) findViewById(R.id.camera_button);
        saveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAdd(view);
            }
        });
        removeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRemove(view);
            }
        });
        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onClickCameraButton(view);
            }
        });

        // Here we define the broadcast receiver which should receive info from IntentService
        // This is a feature that is only implemented here for testing, as activities will not show
        // on the HUD of our app. (maybe for future use? or factory use?). If you want to enable broadcast
        //receiver, go to the IntentService and uncomment the broadcastActivity() method

        // broadcastReceiver here calls onReceive() whenever it receives a intent (with a specific action label) from other sources
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("activities_intent")) {  // action string must match from sender

                    ArrayList<String> a = intent.getStringArrayListExtra("activity");
                    text = intent.getStringExtra("variables");

                    detectedActivitiesList = a;

                    for(String it: detectedActivitiesList){
                        Log.v("HAIDER", " on receive says: "+ it);
                    }
                }
            }
        };

        // Our broadcastReceiver object is being registered here. Repeat this in onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("activity_intent"));

        // this client is being instantiated here. It is responsible for monitoring  activity updates
        mActivityRecognitionClient = new ActivityRecognitionClient(this);




    }


    @Override
    public void onStart(){
        super.onStart();
        // request permission for activity recognition
        requestProbableActivities();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Our broadcastReceiver object is being registered here
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("activity_intent"));
    }

    @Override         // This method attaches icons to our actionbar
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mflater = getMenuInflater();
        mflater.inflate(R.menu.custom_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override      // THIS is the method that adds functionality to each menu item
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.help:   // this launches onboarding again
                Intent introIntent = new Intent(MainActivity.this, OnboardingActivity.class);
                startActivity(introIntent);
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(){
        FileHandler fh = new FileHandler();
        fh.writeToFile(saveFileName, occurrences, this);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Our broadcastReceiver object is being unregistered here
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroy(){

        super.onDestroy();
    }

    // This is the method that initiates the IntentService that will get the probable activities the user is doing
    public void requestProbableActivities() {

        // We need to create an Intent that will launch our IntentService. IntentService is the
        // the service that will specify (in onHandleIntent()) what we should do when we receive a
        // list of probable activities the user is doing (print them or issue notifications for example)
        Intent intent = new Intent(this, DActivitiesIntentService.class);

        PendingIntent pt = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.v("HAIDER", "requestActivity called");
        //Task initiates activity monitoring and will launch the PendingIntent when it has updates.
        // Set the activity detection interval.
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_IN_MILLISECONDS,
                pt);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {

                // on task success is not the actual method that will receive the list of activites
                // becuase that will be the job of IntentService onHandleIntent() method
                Log.v("HAIDER", "Task successful");
            }
        });
    }


    private class SectionsPagerAdapter extends FragmentPagerAdapter{
        private TopFragment topFragment1;
        private TopFragment topFragment2;
        private TopFragment topFragment3;
        private int count = fragmentCount;

        public SectionsPagerAdapter(FragmentManager fm, int count){
            super(fm);
            this.count = count;
        }

        @Override
        public Fragment getItem(int i) {

            switch(i){
                case 0:
                    topFragment1= new TopFragment();
                    topFragment1.setFragmentPosition(occurrences.size()-1);
                    return topFragment1;
                case 1:
                    topFragment2= new TopFragment();
                    topFragment2.setFragmentPosition(occurrences.size()-2);
                    return topFragment2;
                case 2:
                    topFragment3 = new TopFragment();
                    topFragment3.setFragmentPosition(occurrences.size()-3);
                    return topFragment3;
            }
            return null;
        }

        @Override
        public int getCount() {

            return count;
        }
        // Here we can finally safely save a reference to the created
        // Fragment, no matter where it came from (either getItem() or
        // FragmentManger). Simply save the returned Fragment from
        // super.instantiateItem() into an appropriate reference depending
        // on the ViewPager position.
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    topFragment1 = (TopFragment) createdFragment;
                    topFragment1.setFragmentPosition(occurrences.size()-1);
                    break;
                case 1:
                    topFragment2 = (TopFragment) createdFragment;
                    topFragment2.setFragmentPosition(occurrences.size()-2);
                    break;
                case 2:
                    topFragment3 = (TopFragment) createdFragment;
                    topFragment3.setFragmentPosition(occurrences.size()-3);
                    break;
            }
            return createdFragment;
        }
    }


    // An interface method that gets the the textview of the Fragment and sets its text to
    // the retrieved text strings from the Occurrence object. Note this method accesses the occurrences
    // ArrayList to do that.
    @Override
    public void adjustTextView(int id, int fragmentPos, Fragment fragment) {
        // id is used to get the TextView id, and pos is the position of the Occurrence object to
        // be retrieved
        View layout = fragment.getView();
        TextView textView =  (TextView)layout.findViewById(id);
        String retrievedText1 = "Empty";
        String retrievedText2 = "Empty";
        int size = occurrences.size();
        if ( occurrences.size() > 0 && fragmentPos < occurrences.size()) {

            Occurrence selectedInstance = peek(fragmentPos);

            retrievedText1 = selectedInstance.getText1();
            retrievedText2 = selectedInstance.getText2();
            Log.d(" ----- > ", "retrieved text1 "+retrievedText1 +
                    " retrievedText2 "+ retrievedText2);
        }
//        else if(occurrences.size() == fragmentPos) {
//
//            Occurrence selectedInstance = peek(fragmentPos - 1); // Next item in the Array
//            retrievedText1 = selectedInstance.getText1();
//            retrievedText2 = selectedInstance.getText2();
//            Log.d("t", "retrieved text1 "+retrievedText1 +
//                    " retrievedText2 "+ retrievedText2);
//
//        }

        textView.setText(retrievedText1+ ".  "+ retrievedText2);
        Log.d(" ----- > ", " adjustTextView  was  called  \n");

    }
    @Override
    public void adjustImageView(int imageViewId, int fragmentPosition, Fragment fragment) {
        /* similar to previous method, except that this one adjusts the imageview (if not null)
         */
        View fragmentLayout = fragment.getView();
        ImageView imageView =  (ImageView)fragmentLayout.findViewById(imageViewId);
        imageView.setImageResource(R.drawable.noimage);
        if ( occurrences.size() > 0 && fragmentPosition < occurrences.size()
                ) {

            Occurrence selectedInstance = peek(fragmentPosition);  // Picking instance based on fragment position

            String testing = selectedInstance.getimageUriString();


            if( selectedInstance.getimageUriString() != null){

                String retrievedImageUri = selectedInstance.getimageUriString();
                Uri imageUri = Uri.parse(retrievedImageUri);

                Bitmap bitmapImage = retrieveImageAsBitmap(imageUri);

                if(selectedInstance.getimageUriString().contains("/")){
                    if(bitmapImage != null){
                        imageView.setImageBitmap(bitmapImage);
                        Log.d(" ----  > ", "imageView.setImageBitmap(bitmapImage)");
                    }
                    else {
                        imageView.setImageResource(R.drawable.noimage);
                    }
                }

                return;
            }
            else {
                Log.d(TAG, "No imageUri detected in the array");
                imageView.setImageResource(R.drawable.noimage);
            }

        }

    }

    public String retrieveTextFromEditText(int editTextId) {
        /* A helper method that gets the EditText1 and 2 */
        EditText editText = (EditText)findViewById(editTextId);
        String text = editText.getText().toString();
        return text;
    }

    public Occurrence prepareAndAddOccurrence(Occurrence occurrence, int editTextId1, int editTextId2) {
        /* Extract string texts from the EditText views and
        assign them to an occurrence object then push that object to the occurrences array*/

        // Retrieve texts from the EditText
        String text1 = retrieveTextFromEditText(editTextId1);
        String text2 = retrieveTextFromEditText(editTextId2);

        // Set objectToBeAdded text1 and text2
        occurrence.setText1(text1);
        occurrence.setText2(text2);


        Occurrence pushedAwayOccurrence = pushOccurence(occurrence);
        Log.d("MainActivity", occurrences.toString());
        return pushedAwayOccurrence;
    }

    public void createNewPagerAdapter(int viewPagerId) {
/*      A method that is required to refresh the pager when changes occur. It does so by
        creating new PagerAdapter and attaches it to the item with viewPgaerId. This method
        checks first if fragment count is correct to prevent going over the designated count
        */

        if (fragmentCount >= 3){
            Log.d("createNewPagerAdapter", String.valueOf(fragmentCount));
            Log.d("createNewPagerAdapter", "Fragment count is full");
            fragmentCount = 3;
        }
        else {
            fragmentCount = occurrences.size();
            Log.d("createNewPagerAdapter", "Fragment count increased "+ String.valueOf(fragmentCount));
        }

        SectionsPagerAdapter theAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager(), fragmentCount);
        ViewPager pager = (ViewPager)findViewById(viewPagerId);
        pager.setAdapter(theAdapter);
    }



    // ---------------- Button clicks methods ----------------------------------

    // This method retrieves texts from EditText1 and EditText2 in MainActivity, and the
    // pushes a new Occurrence object with those retrieved texts to the occurrences ArrayList

    public  void onClickAdd(View view) {
        Occurrence occurrence = new Occurrence();
        // Call prepareAndAddOccurrence to extract string texts from the EditText views and
        // assign them to an occurrence object then push that object to the occurrences array
         Occurrence pushedAwayOccurrence = prepareAndAddOccurrence(occurrence, R.id.editText1, R.id.editText2);

        // Refresh the ViewPager
        createNewPagerAdapter(R.id.pager);

        // Adding animation to button clicks by calling addAnimation(screenFlashColor)
        addAnimation("#0091EA");

        Toast.makeText(this, "Location added!", Toast.LENGTH_SHORT).show();

        // Delete occurence that is going to go away
        String retrievedImageUri = null;

        if(pushedAwayOccurrence != null){
            retrievedImageUri = pushedAwayOccurrence.getimageUriString();
        }
        if(retrievedImageUri != null && retrievedImageUri.contains("/")) {
            Uri imageUri = Uri.parse(retrievedImageUri);
            long mediaId = ContentUris.parseId(imageUri);
            Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri itemUri = ContentUris.withAppendedId(contentUri, mediaId);

            this.getContentResolver().delete(itemUri, null, null);

        }

    }


    // This method adds a simple background color flash animation when the add or remove button is
    // clicked. The parameter is the color that will flash initially in the background
    public void addAnimation(String startColorHexCode) {
        final String  startColorHex = startColorHexCode;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                View view = findViewById(R.id.topLinearLayout);
                float fractionAnim = (float) valueAnimator.getAnimatedValue();
                // Change starting color (#4CD137 capital letters) and ending color(white)
                view.setBackgroundColor(ColorUtils.blendARGB(Color.parseColor(startColorHex)
                        , Color.parseColor("#FFB39DDB")
                        , fractionAnim));
            }
        });
        valueAnimator.start();
    }


    //------- METHODS FOR CAMERA BUTTON  -----------


    public void onClickCameraButton(View view){
        boolean writePermissionGranted = false;
        Occurrence objectToBeAdded = new Occurrence();
        Occurrence pushedAwayOccurrence = new Occurrence(); // this object should be deleted

        Uri imageUri;

            values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "MyPicture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " +
                    System.currentTimeMillis());


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Log.v("HAIDER", "requestWritePermission is bout to be called");
                // We achieve this through this private method (it is defined below this method)
                // This method is called Async because dialog in it is Async. Thus, onCamerClick() will
                // finish while the dialog remains
                if(dialogAppeared == false) {
                    requestWritePermission();
                }
                Log.v("HAIDER", "AFTER requestWritePermission is bout to be called");



            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                Log.v("HAIDER", "No explanation needed; request the permission");

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            Log.v("HAIDER", "Permission has already been granted");
            // ----> uri
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            currentImageUri = imageUri; // This is done because we need a global imageUri in onActivityResult

            objectToBeAdded.setImageUriString(imageUri.toString()); // Recording the image Uri in the occurrence object
            pushedAwayOccurrence = prepareAndAddOccurrence(objectToBeAdded, R.id.editText1, R.id.editText2);

            //---------- here we delete the occurrence that will be pushed away from occurrences--------

            // Delete occurence that is going to go away
            String retrievedImageUri = null;

            if(pushedAwayOccurrence != null){
                retrievedImageUri = pushedAwayOccurrence.getimageUriString();
            }
            if(retrievedImageUri != null && retrievedImageUri.contains("/")) {
                Uri imageUri2 = Uri.parse(retrievedImageUri);
                long mediaId = ContentUris.parseId(imageUri2);
                Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Uri itemUri = ContentUris.withAppendedId(contentUri, mediaId);

                this.getContentResolver().delete(itemUri, null, null);

            }

           // -----------------------------------------------------------------------------------------


            Log.d("iamgeUri ----- > ", imageUri.toString());
            Log.d("values", values.toString());

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, CAMERA_REQUEST);
            }
            Log.v("HAIDER", "method click camera has finished");
        }

    private void requestWritePermission(){
        new AlertDialog.Builder(this)
                .setTitle("Permission To Save Photos")
                .setMessage("RE-PARK requires permission to save location photos that you take with your camera." +
                        " Denying this permission will prevent you from saving location photos to your device")
                // specify the OK button
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogAppeared = true;
                        // request permission again
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        onClickCameraButton(null);

                    }
                })
                // Specify the negation button
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }
    private void requestReadPermission(){
        new AlertDialog.Builder(this)
                .setTitle("Permission To Retrieve Photos")
                .setMessage("RE-PARK requires permission to retrieve location photos that you have taken with your camera." +
                        " Denying this permission will prevent you from accessing any saved location photos")
                // specify the OK button
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // request permission again
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    }
                })
                // Specify the negation button
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    private void requestActivityReconPermission(){
        new AlertDialog.Builder(this)
                .setTitle("Permission To Save Photos")
                .setMessage("RE-PARK requires permission to save location photos that you take with your camera." +
                        " Denying this permission will prevent you from saving location photos to your device")
                // specify the OK button
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // request permission again
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                    }
                })
                // Specify the negation button
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {


            // Refresh the ViewPager ---> to force Fragment to call adjustImageView()
            // Refresh the ViewPager
            createNewPagerAdapter(R.id.pager);


        } else if (resultCode == RESULT_CANCELED){
            Log.d(" ---- >  ", "Camera taking was canceled");
            return;
        }
    }

    public Bitmap retrieveImageAsBitmap (Uri selectedImageUri) {
        /*
            Retrieves image as bitmap from the storage
         */
        String imageName;
        Bitmap bitmap = null;
        try { // Check permission here?
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                   // requestReadPermission();
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                // Permission has already been granted

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                if (bitmap != null) {
                    Log.d("bitmap", bitmap.toString());
                    Log.d("iamgeUri", selectedImageUri.toString());
                    Log.d("fileName ", getFileName(selectedImageUri));

                    imageName = getFileName(selectedImageUri);
                    //imageNames.add(imageName);

                    Log.d("imageName", imageName);
                }
            }


        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
        return bitmap;
    }

    public String getFileName(Uri uri) {
        // Gets the file name from storage using a Uri
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }



    public void onClickRemove(View view){
        Occurrence objectToBeRemoved = new Occurrence();



        ViewPager oldPager = (ViewPager) findViewById(R.id.pager);
        int currentPage = oldPager.getCurrentItem();

        if (currentPage == 2){
            objectToBeRemoved = removeOccurence((occurrences.size()-1) - 2);
            // Array size is 3 but last item index is (3-1 = 2)
        }
        else if(currentPage == 1){
            objectToBeRemoved = removeOccurence((occurrences.size()-1) - 1);
        }
        else if(currentPage == 0){
            objectToBeRemoved = removeOccurence((occurrences.size()-1) - 0);
        }


        Log.d("MainActivity", occurrences.toString());
        fragmentCount = occurrences.size();
        if (fragmentCount < 3 && fragmentCount > 0){
            Log.d("TAG", String.valueOf(fragmentCount));

            Log.d("TAG", "Fragment count decreased "+ String.valueOf(fragmentCount));

        }
        else {
            fragmentCount = 0;
            Log.d("TAG", "Fragment count is reset to "+ String.valueOf(fragmentCount));

        }
        SectionsPagerAdapter theAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager(), fragmentCount );
        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(theAdapter);


        // Adding animation to button clicks by calling addAnimation(screenFlashColor)
        addAnimation("#e74c3c");
        String retrievedImageUri = null;
        if(objectToBeRemoved != null){
            retrievedImageUri = objectToBeRemoved.getimageUriString();
        }
        if(retrievedImageUri != null && retrievedImageUri.contains("/")) {
            Uri imageUri = Uri.parse(retrievedImageUri);
            long mediaId = ContentUris.parseId(imageUri);
            Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri itemUri = ContentUris.withAppendedId(contentUri, mediaId);

            this.getContentResolver().delete(itemUri, null, null);

        }

        Toast.makeText(this, "Location removed!", Toast.LENGTH_SHORT).show();
    }
    @Override
    public Bitmap getCurrentBitmapImage(){
        return currentBitmapImage;
    }






}
