package net.neoforged.fml

enum class ModLoadingStage {
    ERROR, VALIDATE, CONSTRUCT, COMMON_SETUP, SIDED_SETUP, ENQUEUE_IMC, PROCESS_IMC, COMPLETE, DONE;

    val deferredWorkQueue = DeferredWorkQueue(this)
}