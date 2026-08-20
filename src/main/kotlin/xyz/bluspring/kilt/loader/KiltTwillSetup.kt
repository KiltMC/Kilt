package xyz.bluspring.kilt.loader

import xyz.bluspring.twill.loader.TwillOverrides

class KiltTwillSetup : Runnable {
    override fun run() {
        TwillOverrides.instance = KiltLoader.instance
    }
}
