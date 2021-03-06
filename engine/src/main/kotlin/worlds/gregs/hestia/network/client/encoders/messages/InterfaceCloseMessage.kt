package worlds.gregs.hestia.network.client.encoders.messages

import world.gregs.hestia.core.network.codec.message.Message

/**
 * Closes a client interface
 * @param id The id of the parent interface
 * @param component The index of the component to close
 */
data class InterfaceCloseMessage(val id: Int, val component: Int) : Message