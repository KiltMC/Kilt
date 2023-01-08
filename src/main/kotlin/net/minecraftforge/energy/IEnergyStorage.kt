package net.minecraftforge.energy

interface IEnergyStorage {
    fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int
    fun extractEnergy(maxExtract: Int, simulate: Boolean): Int
    val energyStored: Int
    val maxEnergyStored: Int
    fun canExtract(): Int
    fun canReceive(): Int
}