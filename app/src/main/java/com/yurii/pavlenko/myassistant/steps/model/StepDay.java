package com.yurii.pavlenko.myassistant.steps.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Steps recorded for one calendar day, keyed by its epoch day. */
@Entity(tableName = "step_days")
public class StepDay {

    @PrimaryKey
    private long day;
    private int walkSteps;
    private int runSteps;

    public StepDay(long day, int walkSteps, int runSteps) {
        this.day = day;
        this.walkSteps = walkSteps;
        this.runSteps = runSteps;
    }

    public long getDay() { return day; }
    public int getWalkSteps() { return walkSteps; }
    public int getRunSteps() { return runSteps; }
}
