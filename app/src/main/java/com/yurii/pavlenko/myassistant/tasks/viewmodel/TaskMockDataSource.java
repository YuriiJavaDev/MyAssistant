package com.yurii.pavlenko.myassistant.tasks.viewmodel;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskMockDataSource {

    public static List<Task> getInitialTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(UUID.randomUUID(), "Drink a cup of coffee", false, LocalDateTime.now().minusHours(3), null, null, "Normal", null, false));
        tasks.add(new Task(UUID.randomUUID(), "Read Java textbook", false, LocalDateTime.now().minusHours(2), null, null, "Important", null, false));
        tasks.add(new Task(UUID.randomUUID(), "Sleep before and after lunch", false, LocalDateTime.now().minusHours(1), null, null, "Urgent", null, false));
        return tasks;
    }
}