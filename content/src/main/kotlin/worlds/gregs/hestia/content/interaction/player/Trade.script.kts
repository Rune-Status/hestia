package worlds.gregs.hestia.content.interaction.player

import worlds.gregs.hestia.core.action.Action
import worlds.gregs.hestia.core.display.client.model.events.Chat
import worlds.gregs.hestia.core.display.window.api.Windows.Companion.TradeMain
import worlds.gregs.hestia.core.display.window.logic.systems.RequestSystem
import worlds.gregs.hestia.core.display.window.model.PlayerOptions.TRADE
import worlds.gregs.hestia.core.display.window.model.Request
import worlds.gregs.hestia.core.display.window.model.WindowPane
import worlds.gregs.hestia.core.display.window.model.actions.CloseWindowPane
import worlds.gregs.hestia.core.display.window.model.actions.OpenWindow
import worlds.gregs.hestia.core.display.window.model.events.AcceptedRequest
import worlds.gregs.hestia.core.display.window.model.events.PlayerOption
import worlds.gregs.hestia.core.display.window.model.events.RequestResponse
import worlds.gregs.hestia.core.display.dialogue.model.ChatType.GameTrade

on<PlayerOption> {
    where { option == TRADE }
    fun PlayerOption.task() = queue(TaskPriority.High) {
        entity.interact(target, 1)
        system(RequestSystem::class).sendRequest(entity, target, Request.TRADE)
    }
    then(PlayerOption::task)
}

//The original requester
on<RequestResponse> {
    where { request == Request.TRADE }
    fun RequestResponse.task() = queue(TaskPriority.High) {
        trade(this, target)
    }
    then(RequestResponse::task)
}

//The responder
on<AcceptedRequest> {
    where { request == Request.TRADE }
    fun AcceptedRequest.task() = queue(TaskPriority.High) {
        trade(this, target)
    }
    then(AcceptedRequest::task)
}

fun Action.trade(task: Task, target: Int) {
    task.onCancel {
        entity perform CloseWindowPane(WindowPane.MAIN_SCREEN)
        target perform CloseWindowPane(WindowPane.MAIN_SCREEN)
        entity perform Chat("Trade declined.", GameTrade)
        target perform Chat("Other player declined trade.", GameTrade)
    }
    entity perform OpenWindow(TradeMain)
}