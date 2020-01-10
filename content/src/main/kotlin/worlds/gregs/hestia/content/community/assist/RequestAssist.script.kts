package worlds.gregs.hestia.content.community.assist

import world.gregs.hestia.core.network.protocol.encoders.messages.WidgetComponentText
import world.gregs.hestia.core.services.int
import world.gregs.hestia.core.services.plural
import worlds.gregs.hestia.content.activity.skill.Experience
import worlds.gregs.hestia.content.activity.skill.Skill
import worlds.gregs.hestia.core.action.model.EntityAction
import worlds.gregs.hestia.core.display.client.model.events.Chat
import worlds.gregs.hestia.core.display.dialogue.model.ChatType.GameAssist
import worlds.gregs.hestia.core.display.update.model.components.DisplayName
import worlds.gregs.hestia.core.display.window.api.Windows.Companion.AreaStatusIcon
import worlds.gregs.hestia.core.display.window.api.Windows.Companion.AssistXP
import worlds.gregs.hestia.core.display.window.api.Windows.Companion.FilterButtons
import worlds.gregs.hestia.core.display.window.logic.systems.RequestSystem
import worlds.gregs.hestia.core.display.window.logic.systems.WindowSystem
import worlds.gregs.hestia.core.display.window.model.FilterMode
import worlds.gregs.hestia.core.display.window.model.PlayerOptions.ASSIST
import worlds.gregs.hestia.core.display.window.model.Request
import worlds.gregs.hestia.core.display.window.model.actions.CloseWindow
import worlds.gregs.hestia.core.display.window.model.actions.OpenWindow
import worlds.gregs.hestia.core.display.window.model.components.Assistance
import worlds.gregs.hestia.core.display.window.model.components.Assisting
import worlds.gregs.hestia.core.display.window.model.events.AcceptedRequest
import worlds.gregs.hestia.core.display.window.model.events.PlayerOption
import worlds.gregs.hestia.core.display.window.model.events.RequestResponse
import worlds.gregs.hestia.core.display.window.model.events.WindowInteraction
import worlds.gregs.hestia.core.entity.entity.model.components.Position
import worlds.gregs.hestia.core.entity.entity.model.events.Animate
import worlds.gregs.hestia.core.entity.entity.model.events.Graphic
import worlds.gregs.hestia.core.task.logic.systems.awaitWindow
import worlds.gregs.hestia.core.task.logic.systems.wait
import worlds.gregs.hestia.core.world.movement.model.MovementType
import worlds.gregs.hestia.core.world.movement.model.components.types.Movement
import worlds.gregs.hestia.core.world.movement.model.events.Moved
import worlds.gregs.hestia.game.Engine
import worlds.gregs.hestia.network.client.encoders.messages.ConfigFile
import worlds.gregs.hestia.network.client.encoders.messages.WidgetVisibility
import java.util.concurrent.TimeUnit

val skills = listOf(Skill.RUNECRAFTING, Skill.CRAFTING, Skill.FLETCHING, Skill.CONSTRUCTION, Skill.FARMING, Skill.MAGIC, Skill.SMITHING, Skill.COOKING, Skill.HERBLORE)
val config = listOf(4090, 4091, 4093, 4095, 4096, 4098, 4100, 4101, 4102)
val maximumExperience = 30000
val requestDelay = 10

on<PlayerOption> {
    where { option == ASSIST }
    fun PlayerOption.task() = queue(TaskPriority.High) {
        val assisting = entity get Assisting::class
        //Delayed requesting
        val lastRequest = Engine.ticks - assisting.lastRequest//10 - 5
        if (lastRequest in 1 until requestDelay - 1) {
            val waitTime = requestDelay - lastRequest
            entity perform Chat("You have only just made an assistance request", GameAssist)
            entity perform Chat("You have to wait $waitTime ${"second".plural(waitTime)} before making a new request.", GameAssist)
            return@queue
        }
        val targetAssisting = target get Assisting::class
        update(targetAssisting)
        if (targetAssisting.experienceGained >= maximumExperience) {
            entity perform Chat("${target.get(DisplayName::class).name} is unable to assist at the moment.", GameAssist)//Unconfirmed
            val hours = assisting.getHoursRemaining()
            target perform Chat("An assist request has been refused. You can assist again in $hours ${"hour".plural(hours)}.", GameAssist)
            return@queue
        }
        entity.interact(target, 1)
        system(RequestSystem::class).sendRequest(entity, target, Request.ASSIST)
        assisting.lastRequest = Engine.ticks
    }
    then(PlayerOption::task)
}

//The assistance requester
on<RequestResponse> {
    where { request == Request.ASSIST }
    fun RequestResponse.task() = queue(TaskPriority.High) {
        entity perform Chat("You are being assisted by ${target.get(DisplayName::class).name}.", GameAssist)
        val assistance = entity create Assistance::class
        assistance.helper = target
        assistance.point.set(target get Position::class)
        entity send WidgetVisibility(AreaStatusIcon, 2, false)
        wait(2)
        entity perform Animate(7299)
    }
    then(RequestResponse::task)
}

//The assistance giver
on<AcceptedRequest> {
    where { request == Request.ASSIST }
    fun AcceptedRequest.task() = queue(TaskPriority.High) {
        onCancel { cancel(entity, target) }
        entity perform Chat("You are assisting ${target.get(DisplayName::class).name}.", GameAssist)
        val assisting = entity get Assisting::class
        update(assisting)
        entity perform OpenWindow(AssistXP)
        entity send WidgetComponentText(AssistXP, 10, "The Assist System is available for you to use.")
        entity send WidgetComponentText(AssistXP, 73, "Assist System XP Display - You are assisting ${target.get(DisplayName::class).name}")//TODO there's probably a packet or config for replacing `<name>`
        entity send WidgetVisibility(AreaStatusIcon, 2, false)
        entity send ConfigFile(4103, assisting.experienceGained * 10)
        entity perform Animate(7299)
        entity perform Graphic(1247)
        //TODO disable inventory
        awaitWindow(AssistXP)
        if (system(WindowSystem::class).hasWindow(entity, AssistXP)) {
            cancel(entity, target)
        }
    }
    then(AcceptedRequest::task)
}

//Handle skill toggling
on<WindowInteraction> {
    where { target == AssistXP && widget in 74..82 }
    then {
        val assisting = entity create Assisting::class
        val index = widget - 74
        assisting.skills[index] = !assisting.skills[index]
        entity send ConfigFile(config[index], assisting.skills[index].int)
    }
}

//Check if assisted player moves outside of range
on<Moved> {
    where { entity has Assistance::class }
    then {
        val assistance = entity get Assistance::class
        val position = entity get Position::class
        when (entity.get(Movement::class).actual) {//Movement type
            //Allow teleportation
            MovementType.Move -> assistance.point.set(position)
            else -> {
                //Cancel if player exceeds 20 squares from helper (or teleport point)
                if (!assistance.point.withinDistance(position, 20)) {
                    cancel(assistance.helper, entity)
                }
            }
        }
    }
}

//Filter button handling
on<WindowInteraction> {
    where { target == FilterButtons && widget == 16 }
    then {
        val assisting = entity get Assisting::class
        when (option) {
            1 -> {//View
            }
            2 -> assisting.mode = FilterMode.On
            3 -> assisting.mode = FilterMode.Friends
            4 -> assisting.mode = FilterMode.Off
            9 -> {//Xp Earned/Time
                update(assisting)
                if (assisting.experienceGained >= maximumExperience) {
                    val hours = assisting.getHoursRemaining()
                    entity perform Chat("You've earned the maximum XP (30,000 Xp) from the Assist System within a 24-hour period.", GameAssist)
                    entity perform Chat("You can assist again in $hours ${"hour".plural(hours)}.", GameAssist)
                } else {
                    entity perform Chat("You have earned ${assisting.experienceGained} Xp. The Assist system is available to you.", GameAssist)
                }
            }
        }
    }
}

//Intercepting xp
on<Experience>(1) {
    where { entity has Assistance::class }
    then {
        val assistance = entity get Assistance::class
        val target = assistance.helper
        val assisting = target get Assisting::class
        val index = skills.indexOf(skill)
        //If skill is being assisted
        if (index != -1 && assisting.skills[index] && assisting.experienceGained < maximumExperience) {
            assisting.experienceGained += increase
            if (assisting.experienceGained >= maximumExperience) {
                target send WidgetComponentText(AssistXP, 10, "You've earned the maximum XP from the Assist System with a 24-hour period.\nYou can assist again in 24 hours.")
                assisting.timeout = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)
            }
            //TODO what if increase > maximum or entity level > targets
            target perform Experience(skill, increase)
            target send ConfigFile(4103, assisting.experienceGained * 10)
            isCancelled = true
        }
    }
}

/**
 * Returns the number of hours remaining before able to assist again
 */
fun Assisting.getHoursRemaining(): Int {
    val remainingTime = System.currentTimeMillis() - timeout
    return TimeUnit.MILLISECONDS.toHours(remainingTime).toInt()
}

/**
 * Checks to see if the 24 hour timeout has passed
 */
fun EntityAction.update(assisting: Assisting) {
    if (assisting.timeout <= 0) {
        return
    }
    val remainingTime = System.currentTimeMillis() - assisting.timeout
    if (remainingTime <= 0) {
        assisting.experienceGained = 0
        assisting.timeout = 0
        entity perform Chat("It has been 24 hours since you first helped someone using the Assist System.", GameAssist)
        entity perform Chat("You can now use it to gain the full amount of XP.", GameAssist)
    }
}

/**
 * Cancels the current assistance
 * Caused by either giver closing/interrupted window or requester moving over 20 tiles away
 * @param entity The helper
 * @param target The assisted
 */
fun EntityAction.cancel(entity: Int, target: Int) {
    entity perform CloseWindow(AssistXP)
    entity perform Chat("You have stopped assisting ${target.get(DisplayName::class).name}.", GameAssist)
    target perform Chat("${entity.get(DisplayName::class).name} has stopped assisting you.", GameAssist)//Unconfirmed
    entity send WidgetVisibility(AreaStatusIcon, 2, true)
    target send WidgetVisibility(AreaStatusIcon, 2, true)
    target remove Assistance::class
}