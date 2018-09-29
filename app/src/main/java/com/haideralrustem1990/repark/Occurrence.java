package com.haideralrustem1990.repark;

public class Occurrence {
    private String text1;
    private String text2;
    private String imageUri;

    public Occurrence(){
    }
    public Occurrence(String text1, String text2, String imageUri){
        this.text1 = text1;
        this.text2 = text2;
        this.imageUri = imageUri;

    }

    public String getText1() {
        return text1;
    }
    public String getText2() {

        return text2;
    }
    public String getimageUriString() {
        return imageUri;}

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public void setImageUriString(String imageUri){
        this.imageUri = imageUri;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }
    @Override
    public String toString(){
        return this.text1+ " \\ " + this.text2 + " \\ "+ this.imageUri;
    }
}
