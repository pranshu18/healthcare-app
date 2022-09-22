package com.example.assignment2;

public class Person {
    private static String name="";
    private static float heartRate=0;
    private static float respRate=0;
    private static float nausea=0;
    private static float headache=0;
    private static float diarrhea=0;
    private static float soreThroat=0;
    private static float fever=0;
    private static float muscleAche=0;
    private static float lossOfSmellOrTaste=0;
    private static float cough=0;
    private static float shortnessOfBreath=0;
    private static float feelingTired=0;
    private static float longitude=0;
    private static float latitude=0;

    public static float getLongitude() {
        return longitude;
    }

    public static void setLongitude(double longitude) {
        Person.longitude = (float) longitude;
    }

    public static float getLatitude() {
        return latitude;
    }

    public static void setLatitude(double latitude) {
        Person.latitude = (float) latitude;
    }

    public static float getHeartRate() {
        return heartRate;
    }

    public static void setHeartRate(float heartRate) {
        Person.heartRate = heartRate;
    }

    public static float getRespRate() {
        return respRate;
    }

    public static void setRespRate(float respRate) {
        Person.respRate = respRate;
    }

    public static float getNausea() {
        return nausea;
    }

    public static void setNausea(float nausea) {
        Person.nausea = nausea;
    }

    public static float getHeadache() {
        return headache;
    }

    public static void setHeadache(float headache) {
        Person.headache = headache;
    }

    public static float getDiarrhea() {
        return diarrhea;
    }

    public static void setDiarrhea(float diarrhea) {
        Person.diarrhea = diarrhea;
    }

    public static float getSoreThroat() {
        return soreThroat;
    }

    public static void setSoreThroat(float soreThroat) {
        Person.soreThroat = soreThroat;
    }

    public static float getFever() {
        return fever;
    }

    public static void setFever(float fever) {
        Person.fever = fever;
    }

    public static float getMuscleAche() {
        return muscleAche;
    }

    public static void setMuscleAche(float muscleAche) {
        Person.muscleAche = muscleAche;
    }

    public static float getLossOfSmellOrTaste() {
        return lossOfSmellOrTaste;
    }

    public static void setLossOfSmellOrTaste(float lossOfSmellOrTaste) {
        Person.lossOfSmellOrTaste = lossOfSmellOrTaste;
    }

    public static float getCough() {
        return cough;
    }

    public static void setCough(float cough) {
        Person.cough = cough;
    }

    public static float getShortnessOfBreath() {
        return shortnessOfBreath;
    }

    public static void setShortnessOfBreath(float shortnessOfBreath) {
        Person.shortnessOfBreath = shortnessOfBreath;
    }

    public static float getFeelingTired() {
        return feelingTired;
    }

    public static void setFeelingTired(float feelingTired) {
        Person.feelingTired = feelingTired;
    }

    public static void setName(String name){
        Person.name=name;
    }

    public static String getName() {
        return name;
    }

}
