package worlds.gregs.hestia.core.display.client.logic.systems.network.`in`

import worlds.gregs.hestia.GameServer
import worlds.gregs.hestia.core.display.window.api.Windows
import worlds.gregs.hestia.core.display.window.api.Windows.Companion.WorldMap
import worlds.gregs.hestia.game.entity.MessageHandlerSystem
import worlds.gregs.hestia.network.client.decoders.messages.WorldMapOpen

class WorldMapOpenHandler : MessageHandlerSystem<WorldMapOpen>() {

    override fun initialize() {
        super.initialize()
        GameServer.gameMessages.bind(this)
    }

    private lateinit var windows: Windows

    override fun handle(entityId: Int, message: WorldMapOpen) {
        windows.openWindow(entityId, WorldMap)
    }

}