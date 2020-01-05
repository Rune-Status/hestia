package worlds.gregs.hestia.content.activity.achievement

import worlds.gregs.hestia.core.display.window.api.Windows.Companion.TaskList
import worlds.gregs.hestia.core.display.window.api.Windows.Companion.TaskSystem
import worlds.gregs.hestia.core.display.window.model.events.WindowInteraction

on<WindowInteraction> {
    where { target == TaskSystem }
    task {
        val (_, _, widget, _, _, _) = event(this)
        when(widget) {
            99, 142, 147, 152, 157, 162 -> {//Select task
                val index = if(widget == 99) 0 else (widget - 137)/5
            }
            102 -> entity openWindow TaskList
        }
    }
}