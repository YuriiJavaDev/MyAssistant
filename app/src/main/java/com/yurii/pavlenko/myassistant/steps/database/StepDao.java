package com.yurii.pavlenko.myassistant.steps.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.yurii.pavlenko.myassistant.steps.logic.DayTotals;
import com.yurii.pavlenko.myassistant.steps.model.StepDay;
import com.yurii.pavlenko.myassistant.steps.model.StepSettings;

import java.util.List;

@Dao
public abstract class StepDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insertDayIfMissing(StepDay day);

    @Query("UPDATE step_days SET walkSteps = walkSteps + :walk, runSteps = runSteps + :run WHERE day = :day")
    abstract void increase(long day, int walk, int run);

    @Transaction
    public void addSteps(long day, int walk, int run) {
        insertDayIfMissing(new StepDay(day, 0, 0));
        increase(day, walk, run);
    }

    @Query("SELECT day AS epochDay, walkSteps, runSteps FROM step_days WHERE day BETWEEN :from AND :to ORDER BY day DESC")
    public abstract LiveData<List<DayTotals>> observeDays(long from, long to);

    @Query("SELECT day AS epochDay, walkSteps, runSteps FROM step_days WHERE day = :day")
    public abstract LiveData<DayTotals> observeDay(long day);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void saveSettings(StepSettings settings);

    @Query("SELECT * FROM step_settings WHERE id = 1")
    public abstract StepSettings settings();

    @Query("SELECT * FROM step_settings WHERE id = 1")
    public abstract LiveData<StepSettings> observeSettings();
}
