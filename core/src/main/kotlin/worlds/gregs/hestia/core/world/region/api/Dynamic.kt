package worlds.gregs.hestia.core.world.region.api

import net.mostlyoriginal.api.system.core.PassiveSystem
import worlds.gregs.hestia.core.world.region.model.components.DynamicRegion

abstract class Dynamic : PassiveSystem() {

    /**
     * @param entityId The region entity to check
     * @return Whether the entity is a dynamic region
     */
    abstract fun isDynamic(entityId: Int): Boolean

    /**
     * @param entityId The entity to make dynamic
     * @return The dynamic region created
     */
    abstract fun create(entityId: Int): DynamicRegion

    /**
     * @param entityId The region entity to find
     * @return The dynamic region
     */
    abstract fun get(entityId: Int): DynamicRegion?
}