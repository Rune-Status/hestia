package world.gregs.hestia.network.update.player

import com.artemis.ComponentMapper
import world.gregs.hestia.game.component.update.ForceChat
import world.gregs.hestia.game.update.UpdateEncoder
import world.gregs.hestia.core.network.packets.Packet

class ForceChatMask(private val forceChatMapper: ComponentMapper<ForceChat>) : UpdateEncoder {

    override val encode: Packet.Builder.(Int, Int) -> Unit = { _, other ->
        writeString(forceChatMapper.get(other)?.message ?: "")
    }

}