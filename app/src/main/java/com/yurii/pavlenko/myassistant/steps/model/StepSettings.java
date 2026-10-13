package com.yurii.pavlenko.myassistant.steps.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.yurii.pavlenko.myassistant.steps.logic.StepLengths;

/** The single settings row; it lives in the step database so settings travel with its backup. */
@Entity(tableName = "step_settings")
public class StepSettings {

    public static final int ROW_ID = 1;

    @PrimaryKey
    private int id;
    private double walkCm;
    private double runCm;
    private int runCadence;

    public StepSettings(int id, double walkCm, double runCm, int runCadence) {
        this.id = id;
        this.walkCm = walkCm;
        this.runCm = runCm;
        this.runCadence = runCadence;
    }

    public static StepSettings from(StepLengths lengths) {
        return new StepSettings(ROW_ID, lengths.getWalkCm(), lengths.getRunCm(), lengths.getRunCadence());
    }

    public StepLengths toLengths() {
        return new StepLengths(walkCm, runCm, runCadence);
    }

    public int getId() { return id; }
    public double getWalkCm() { return walkCm; }
    public double getRunCm() { return runCm; }
    public int getRunCadence() { return runCadence; }
}
