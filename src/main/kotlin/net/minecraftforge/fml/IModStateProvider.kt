package net.minecraftforge.fml

interface IModStateProvider {
    val allStates: List<IModLoadingState>
}