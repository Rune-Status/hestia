package worlds.gregs.hestia.core.task.model.await

import kotlinx.coroutines.CancellableContinuation
import worlds.gregs.hestia.artemis.InstantEvent
import worlds.gregs.hestia.core.action.model.EntityAction
import worlds.gregs.hestia.core.task.api.TaskCancellation
import worlds.gregs.hestia.core.task.api.TaskSuspension

data class ClearTasks(val priority: Int = -1, val cause: TaskCancellation? = null) : EntityAction(), TaskSuspension<Unit>, InstantEvent {
    override lateinit var continuation: CancellableContinuation<Unit>
}