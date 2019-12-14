package worlds.gregs.hestia.core.world.movement.api.systems

import com.artemis.Entity
import com.artemis.WorldConfigurationBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import worlds.gregs.hestia.core.entity.entity.logic.EntityFactory
import worlds.gregs.hestia.core.entity.entity.logic.move
import worlds.gregs.hestia.core.entity.entity.logic.step
import worlds.gregs.hestia.core.entity.entity.model.components.Position
import worlds.gregs.hestia.core.entity.entity.model.components.Position.Companion.regionId
import worlds.gregs.hestia.core.world.movement.MovementPlugin
import worlds.gregs.hestia.core.world.movement.api.Mobile
import worlds.gregs.hestia.core.world.region.RegionPlugin
import worlds.gregs.hestia.core.world.region.logic.RegionFactory
import worlds.gregs.hestia.core.world.region.model.events.CreateRegion
import worlds.gregs.hestia.GameTest
import worlds.gregs.hestia.game.plugin.Plugin.Companion.PRE_SHIFT_PRIORITY
import worlds.gregs.hestia.artemis.dependsOn

internal class ChunkChangedTest : GameTest(WorldConfigurationBuilder().dependsOn(RegionPlugin::class, MovementPlugin::class).with(PRE_SHIFT_PRIORITY, ChunkChange())) {

    private class ChangedChunk : Exception()
    private class ChunkChange : ChunkChanged(Mobile::class) {
        override fun changedChunk(entityId: Int, from: Int, to: Int) {
            throw ChangedChunk()
        }
    }

    @BeforeEach
    override fun setup() {
        super.setup()
        EntityFactory.add(RegionFactory())
    }

    private fun create(x: Int, y: Int, plane: Int = 0): Entity {
        es.dispatch(CreateRegion(regionId(0, 0)))
        val entity = world.createEntity()
        entity.edit().add(Position(x, y, plane)).add(Mobile())
        tick()
        return entity
    }

    @Test
    fun movedLocal() {
        val entity = create(7, 0)
        entity.move(8, 0, 0)
        assertThrows<ChangedChunk> {
            tick()
        }
    }

    @Test
    fun movedRegion() {
        val entity = create(63, 0)
        entity.move(64, 0, 0)
        assertThrows<ChangedChunk> {
            tick()
        }
    }

    @Test
    fun navigate() {
        val entity = create(7, 0)
        entity.step(8, 0, 0)
        assertThrows<ChangedChunk> {
            tick()
        }
    }

    @Test
    fun navigateRegion() {
        val entity = create(63, 0)
        entity.step(64, 0, 0)
        assertThrows<ChangedChunk> {
            tick()
        }
    }
}