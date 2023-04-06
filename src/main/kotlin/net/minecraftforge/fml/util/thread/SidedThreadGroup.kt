package net.minecraftforge.fml.util.thread

import net.minecraftforge.fml.LogicalSide
import java.util.concurrent.ThreadFactory

class SidedThreadGroup(val side: LogicalSide) : ThreadGroup(side.name), ThreadFactory {
    override fun newThread(runnable: Runnable): Thread {
        return Thread(this, runnable)
    }
}