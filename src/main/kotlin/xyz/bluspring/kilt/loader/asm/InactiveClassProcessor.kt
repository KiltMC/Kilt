package xyz.bluspring.kilt.loader.asm

import net.neoforged.neoforgespi.transformation.ClassProcessor
import net.neoforged.neoforgespi.transformation.ProcessorName

object InactiveClassProcessor : ClassProcessor {
    override fun name(): ProcessorName = ProcessorName("kilt", "inactive")
    override fun handlesClass(context: ClassProcessor.SelectionContext?): Boolean = false
    override fun processClass(context: ClassProcessor.TransformationContext?): ClassProcessor.ComputeFlags? = null
}
