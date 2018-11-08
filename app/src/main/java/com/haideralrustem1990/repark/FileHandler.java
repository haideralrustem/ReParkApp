package com.haideralrustem1990.repark;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FileHandler {
    int maxNumberOfObjects = 3;

    public ArrayList<Occurrence> loadStringIntoArrayList(String linesReadFromFile) {

        ArrayList<Occurrence> array = new ArrayList<>(3);
        String lines[] = linesReadFromFile.split("\\r?\\n");
        Log.d("lines length", String.valueOf(lines.length));
        int dynamicNumberOfObjects = 0;

        if(lines.length != 0){
            dynamicNumberOfObjects =  lines.length / 3;

            /* variable dynamicNumberOfObjects is not necessarily always 3(max capacity),
            as it is supposed to predict how many objects will the array have based on number of lines.
            it does so by checking the number of lines based on the multiples of 3 (3, 6, 9) and then
            evaluates number of objects to (1, 2, 3) respectively*/

        }


        int nextItem = lines.length-1;  // this will evaluate to 8 or 5 or 2


        if(lines.length > 3 * maxNumberOfObjects || lines.length < 3){  // This is incorporated in case of file not storing string properly
            String [] defaultLines = {"Empty", "Empty", "Empty",
                        "Empty", "Empty", "Empty",
                        "Empty", "Empty", "Empty"};
            lines = defaultLines;
            nextItem = lines.length-1;
            dynamicNumberOfObjects = maxNumberOfObjects;
        }

        Log.d(" NEXT ITEM ", String.valueOf(nextItem));

        for (int i = 0; i < dynamicNumberOfObjects; i++){

            Log.d(" ", String.valueOf(nextItem));
            String imageUri = lines[nextItem--];
            String text2 = lines[nextItem--];
            String text1 = lines[nextItem--];

            Occurrence event = new Occurrence(text1, text2, imageUri);
            Log.d("occurence before adding", event.toString());
            array.add(event);
        }
        return array;
    }

    public String extractTextDataFromArray(ArrayList<Occurrence> sourceList){
        StringBuilder resultString = new StringBuilder();
        int index=sourceList.size()-1;
        for(int i = index; i >= 0; i--){
            Occurrence occurrence = sourceList.get(i);
            resultString.append(occurrence.getText1());
            resultString.append("\n");
            resultString.append(occurrence.getText2());
            resultString.append("\n");
            resultString.append(occurrence.getimageUriString());
            resultString.append("\n");
        }
        Log.d("RESULT STRING   : ", resultString.toString());
        return resultString.toString();
    }


    public void writeToFile(String fileName, ArrayList<Occurrence> sourceList, Context context) {
        FileOutputStream fos = null;
        String stringData;
        stringData = extractTextDataFromArray(sourceList);

        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(stringData.getBytes());
            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Occurrence> readFromFile(String fileName, Context context) {
        /* A method that reads filecontents and put them in a string. The string
         * is then used in the loadStringIntoArrayList to get an array with Occurrences
          * that represent the string text value*/

        ArrayList<Occurrence> arrayWithLoadedValues = new ArrayList<>(3);
        int numberOfObjects;
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            // READ STRING OF UNKNOWN LENGTH
            StringBuilder sb = new StringBuilder();
            char[] inputBuffer = new char[2048];
            int l;

            // FILL BUFFER WITH DATA
            while ((l = isr.read(inputBuffer)) != -1) {
                sb.append(inputBuffer, 0, l);
            }
            // CONVERT BYTES TO STRING
            String readString = sb.toString();
            fis.close();
            Log.d("READSTRING   : ", readString);
            arrayWithLoadedValues = loadStringIntoArrayList(readString);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return arrayWithLoadedValues;
    }

    public boolean checkIfFileExists(String fileName, Context context){
        File file = ((Activity)context).getBaseContext().getFileStreamPath(fileName);
        return file.exists();
    }


}
