package com.example.application

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TaskEntityTest {

    @Test
    fun defaultTask_isUnsyncedAndIncomplete() {
        val task = TaskEntity(
            title = "Prepare Module Report",
            description = "Finalize system architecture documentation"
        )

        assertFalse(task.isCompleted)
        assertFalse(task.isSynced)
        assertEquals("General", task.category)
        assertEquals("Medium", task.priority)
    }
}