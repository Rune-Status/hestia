package worlds.gregs.hestia.core.display.dialogue.logic.systems.types

import com.artemis.annotations.Wire
import net.mostlyoriginal.api.event.common.Subscribe
import org.slf4j.LoggerFactory
import worlds.gregs.hestia.core.display.dialogue.api.Dialogue
import worlds.gregs.hestia.core.display.dialogue.model.events.IntegerEntered
import worlds.gregs.hestia.core.display.dialogue.logic.systems.DialogueBaseSystem
import worlds.gregs.hestia.core.task.model.events.ProcessDeferral
import worlds.gregs.hestia.game.task.TaskScope
import worlds.gregs.hestia.network.client.encoders.messages.Script
import worlds.gregs.hestia.artemis.send

data class IntegerEntryDialogue(val title: String) : Dialogue {
    var entry: Int? = null
}

suspend fun TaskScope.intEntry(title: String): Int {
    val dialog = IntegerEntryDialogue(title)
    deferral = dialog
    defer()
    return dialog.entry!!
}

@Wire(injectInherited = true)
class IntegerEntryDialogueSystem : DialogueBaseSystem() {

    private val logger = LoggerFactory.getLogger(IntegerEntryDialogueSystem::class.java)!!

    @Subscribe(ignoreCancelledEvents = true)
    private fun handleDefer(event: ProcessDeferral) {
        val (entityId, dialogue) = event
        if(dialogue is StringEntryDialogue) {
            es.send(entityId, Script(109, dialogue.title))
            event.isCancelled = true
        }
    }

    @Subscribe
    private fun handleInput(event: IntegerEntered) {
        val (entityId, integer) = event
        val deferral = getDeferral(entityId)
        val dialogue = deferral as? IntegerEntryDialogue ?: return logger.debug("Task not expected $deferral $event")
        //Set string entered
        dialogue.entry = integer
        //Continue
        taskQueue.resume(entityId)
    }

}