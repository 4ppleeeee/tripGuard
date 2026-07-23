package com.tencent.kmm.startup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.time.TimeSource

class StartupEngine {

    private val tasks = mutableListOf<StartupTask>()
    private val afterAllTasks = mutableListOf<StartupTask>()
    private val listeners = mutableListOf<StartupListener>()

    fun addTask(task: StartupTask): StartupEngine {
        tasks.add(task)
        return this
    }

    fun addTasks(vararg task: StartupTask): StartupEngine {
        tasks.addAll(task)
        return this
    }

    fun addAfterAllTask(task: StartupTask): StartupEngine {
        afterAllTasks.add(task)
        return this
    }

    fun addAfterAllTasks(vararg task: StartupTask): StartupEngine {
        afterAllTasks.addAll(task)
        return this
    }

    fun addListener(listener: StartupListener): StartupEngine {
        listeners.add(listener)
        return this
    }

    fun launch(context: StartupContext): StartupResult = runBlocking {
        launchSuspend(context)
    }

    suspend fun launchSuspend(context: StartupContext): StartupResult {
        val allTasks = collectAllTasks()
        listeners.forEach { it.onStartupBegin(allTasks.size) }

        val startupMark = TimeSource.Monotonic.markNow()
        val executionPlan = topologicalSort(allTasks)
        val taskDurations = linkedMapOf<String, Long>()
        val failures = linkedMapOf<String, Throwable>()

        executionPlan.forEach { layer ->
            val syncTasks = layer.filter { it.scope() == StartupScope.MAIN }
            val asyncTasks = layer.filter { it.scope() == StartupScope.ASYNC }

            syncTasks.forEach { task ->
                applyTaskExecution(executeTask(task, context), taskDurations, failures)
            }

            val asyncResults = coroutineScope {
                asyncTasks.map { task ->
                    async(Dispatchers.Default) {
                        executeTask(task, context)
                    }
                }.awaitAll()
            }
            asyncResults.forEach { result ->
                applyTaskExecution(result, taskDurations, failures)
            }
        }

        afterAllTasks.forEach { task ->
            applyTaskExecution(executeTask(task, context), taskDurations, failures)
        }

        val result = StartupResult(
            totalCount = allTasks.size + afterAllTasks.size,
            successCount = allTasks.size + afterAllTasks.size - failures.size,
            failedCount = failures.size,
            totalDurationMs = startupMark.elapsedNow().inWholeMilliseconds,
            taskDurations = taskDurations.toMap(),
            failures = failures.toMap()
        )
        listeners.forEach { it.onStartupCompleted(result) }
        return result
    }

    private fun collectAllTasks(): List<StartupTask> {
        val result = mutableListOf<StartupTask>()
        result.addAll(tasks)

        val duplicated = result.groupBy { it.taskId }.filterValues { it.size > 1 }.keys
        if (duplicated.isNotEmpty()) {
            throw IllegalStateException("存在重复任务ID: ${duplicated.joinToString()}")
        }

        return result
    }

    private suspend fun executeTask(task: StartupTask, context: StartupContext): TaskExecution {
        listeners.forEach { it.onTaskBegin(task.taskId, task.scope()) }
        val mark = TimeSource.Monotonic.markNow()

        return try {
            task.execute(context)
            val duration = mark.elapsedNow().inWholeMilliseconds
            listeners.forEach { it.onTaskCompleted(task.taskId, duration) }
            TaskExecution.Success(task.taskId, duration)
        } catch (error: Throwable) {
            val duration = mark.elapsedNow().inWholeMilliseconds
            listeners.forEach { it.onTaskFailed(task.taskId, error) }
            TaskExecution.Failure(task.taskId, duration, error)
        }
    }

    private fun applyTaskExecution(
        taskExecution: TaskExecution,
        durations: MutableMap<String, Long>,
        failures: MutableMap<String, Throwable>
    ) {
        when (taskExecution) {
            is TaskExecution.Success -> {
                durations[taskExecution.taskId] = taskExecution.durationMs
            }

            is TaskExecution.Failure -> {
                durations[taskExecution.taskId] = taskExecution.durationMs
                failures[taskExecution.taskId] = taskExecution.error
            }
        }
    }

    private fun topologicalSort(tasks: List<StartupTask>): List<List<StartupTask>> {
        if (tasks.isEmpty()) {
            return emptyList()
        }

        val taskMap = LinkedHashMap<String, StartupTask>()
        val inDegree = LinkedHashMap<String, Int>()
        val adjacency = LinkedHashMap<String, MutableList<String>>()

        tasks.forEach { task ->
            taskMap[task.taskId] = task
            inDegree.put(task.taskId, 0)
        }

        tasks.forEach { task ->
            task.dependencies().forEach { dependency ->
                if (!taskMap.containsKey(dependency)) {
                    throw IllegalStateException("任务 ${task.taskId} 依赖不存在: $dependency")
                }
                adjacency.getOrPut(dependency) { mutableListOf() }.add(task.taskId)
                inDegree[task.taskId] = (inDegree[task.taskId] ?: 0) + 1
            }
        }

        val layers = mutableListOf<List<StartupTask>>()
        var queue = inDegree
            .filterValues { it == 0 }
            .keys
            .toMutableList()

        while (queue.isNotEmpty()) {
            val currentLayer = queue.map { taskMap.getValue(it) }
            layers.add(currentLayer)

            val nextQueue = mutableListOf<String>()
            queue.forEach { taskId ->
                adjacency[taskId].orEmpty().forEach { nextTaskId ->
                    val degree = (inDegree[nextTaskId] ?: 0) - 1
                    inDegree[nextTaskId] = degree
                    if (degree == 0) {
                        nextQueue.add(nextTaskId)
                    }
                }
            }
            queue = nextQueue
        }

        val sortedCount = layers.sumOf { it.size }
        if (sortedCount != tasks.size) {
            val sortedTaskIds = layers.flatten().map { it.taskId }.toSet()
            val cycleTasks = taskMap.keys - sortedTaskIds
            throw IllegalStateException("检测到循环依赖: ${cycleTasks.joinToString()}")
        }

        return layers
    }
}

private sealed interface TaskExecution {
    val taskId: String
    val durationMs: Long

    data class Success(
        override val taskId: String,
        override val durationMs: Long
    ) : TaskExecution

    data class Failure(
        override val taskId: String,
        override val durationMs: Long,
        val error: Throwable
    ) : TaskExecution
}
