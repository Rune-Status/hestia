package world.gregs.hestia.game.archetypes

import com.artemis.ArchetypeBuilder

interface ArchetypeFactory {
    fun getBuilder(): ArchetypeBuilder
}