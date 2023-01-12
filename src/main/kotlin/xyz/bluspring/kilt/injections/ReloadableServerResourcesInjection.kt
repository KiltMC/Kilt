package xyz.bluspring.kilt.injections

import net.minecraftforge.common.crafting.conditions.ICondition

interface ReloadableServerResourcesInjection {
    val conditionContext: ICondition.IContext
}