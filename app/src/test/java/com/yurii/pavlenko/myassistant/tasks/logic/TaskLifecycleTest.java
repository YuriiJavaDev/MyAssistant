package com.yurii.pavlenko.myassistant.tasks.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import org.junit.Test;

import java.time.LocalDateTime;

public class TaskLifecycleTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 18, 12, 30);

    private static Task openTask(LocalDateTime reminder) {
        return new Task("Pay rent", "Normal", null, false, false, false, reminder);
    }

    @Test
    public void completingStampsBothCompletionAndEditTime() {
        Task task = openTask(null);

        TaskLifecycle.setCompleted(task, true, NOW);

        assertTrue(task.isCompleted());
        assertEquals(NOW, task.getCompletedAt());
        assertEquals(NOW, task.getUpdatedAt());
    }

    @Test
    public void reopeningClearsTheCompletionTimeButStampsTheEdit() {
        Task task = openTask(null);
        TaskLifecycle.setCompleted(task, true, NOW.minusDays(1));

        TaskLifecycle.setCompleted(task, false, NOW);

        assertFalse(task.isCompleted());
        assertNull(task.getCompletedAt());
        assertEquals(NOW, task.getUpdatedAt());
    }

    @Test
    public void alarmIsNeededOnlyForAnOpenTaskWithAReminder() {
        assertTrue(TaskLifecycle.needsAlarm(openTask(NOW.plusHours(1))));
        assertFalse(TaskLifecycle.needsAlarm(openTask(null)));

        Task completed = openTask(NOW.plusHours(1));
        TaskLifecycle.setCompleted(completed, true, NOW);
        assertFalse(TaskLifecycle.needsAlarm(completed));
    }
}
