package world.gregs.hestia.game.systems.sync

import com.artemis.Component
import com.artemis.ComponentMapper
import com.artemis.systems.IteratingSystem
import world.gregs.hestia.game.component.map.Position
import world.gregs.hestia.game.component.map.ViewDistance
import world.gregs.hestia.game.component.map.Viewport
import world.gregs.hestia.services.Aspect
import world.gregs.hestia.services.getSystem
import kotlin.reflect.KClass

/**
 * This system prioritises keeping a square viewport over displaying the maximum entities possible
 */

abstract class ViewDistanceSystem(private val chunkSystem: KClass<out ChunkSystem>, private val maxViewDistance: Int, private val maxLocalEntities: Int, viewDistance: KClass<out Component>) : IteratingSystem(Aspect.all(Viewport::class, viewDistance)) {

    private lateinit var positionMapper: ComponentMapper<Position>
    private var array = arrayOfNulls<Int>(maxViewDistance)

    abstract fun getMapper(): ComponentMapper<out Component>

    override fun process(entityId: Int) {
        val viewDistance = getMapper().get(entityId)

        //Reset
        for (index in 0 until array.size) {
            array[index] = null
        }

        val position = positionMapper.get(entityId)

        //For all entities
        world.getSystem(chunkSystem).get(positionMapper.get(entityId))
                .forEach {
                    //Calculate distance
                    val distance = position.getDistance(positionMapper.get(it))
                    if (distance < maxViewDistance) {
                        array[distance] = (array[distance] ?: 0) + 1
                    }
                }

        var count = 0
        var distance = maxViewDistance
        //Calculate the maximum possible view distance
        for (index in 0 until array.size) {
            count += array[index] ?: 0

            if (count > maxLocalEntities) {
                distance = index - 1
                break
            }
        }

        (viewDistance as? ViewDistance)?.distance = distance
    }

    companion object {
        const val DEFAULT_VIEW_DISTANCE = 15
    }
}
