package xyz.bluspring.kilt.workarounds

data class RenderLayerData(
    var limbSwing: Float = 0f, var limbSwingAmount: Float = 0f,
    var partialTicks: Float = 0f,
    var ageInTicks: Float = 0f, var netHeadYaw: Float = 0f,
    var headPitch: Float = 0f
) {
    fun update(limbSwing: Float, limbSwingAmount: Float, partialTicks: Float, ageInTicks: Float, netHeadYaw: Float, headPitch: Float) {
        this.limbSwing = limbSwing
        this.limbSwingAmount = limbSwingAmount
        this.partialTicks = partialTicks
        this.ageInTicks = ageInTicks
        this.netHeadYaw = netHeadYaw
        this.headPitch = headPitch
    }
}