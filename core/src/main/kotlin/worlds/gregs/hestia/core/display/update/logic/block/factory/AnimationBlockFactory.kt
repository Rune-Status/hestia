package worlds.gregs.hestia.core.display.update.logic.block.factory

import com.artemis.ComponentMapper
import worlds.gregs.hestia.artemis.Aspect
import worlds.gregs.hestia.artemis.one
import worlds.gregs.hestia.core.display.update.api.BlockFactory
import worlds.gregs.hestia.core.display.update.model.components.Renderable
import worlds.gregs.hestia.core.display.update.model.components.anim.FirstAnimation
import worlds.gregs.hestia.core.display.update.model.components.anim.FourthAnimation
import worlds.gregs.hestia.core.display.update.model.components.anim.SecondAnimation
import worlds.gregs.hestia.core.display.update.model.components.anim.ThirdAnimation
import worlds.gregs.hestia.game.update.blocks.AnimationBlock

open class AnimationBlockFactory(flag: Int, npc: Boolean = false) : BlockFactory<AnimationBlock>(Aspect.all(Renderable::class).one(FirstAnimation::class, SecondAnimation::class, ThirdAnimation::class, FourthAnimation::class), flag = flag, npc = npc) {

    private lateinit var firstAnimationMapper: ComponentMapper<FirstAnimation>
    private lateinit var secondAnimationMapper: ComponentMapper<SecondAnimation>
    private lateinit var thirdAnimationMapper: ComponentMapper<ThirdAnimation>
    private lateinit var fourthAnimationMapper: ComponentMapper<FourthAnimation>

    override fun create(player: Int, other: Int): AnimationBlock {
        return AnimationBlock(flag, firstAnimationMapper.get(other)?.id
                ?: -1, secondAnimationMapper.get(other)?.id ?: -1, thirdAnimationMapper.get(other)?.id
                ?: -1, fourthAnimationMapper.get(other)?.id ?: -1, firstAnimationMapper.get(other)?.speed ?: 0)
    }

}