package xyz.bluspring.kilt.compat.fabric.sodium;

public interface ModelQuadViewExtension {

    default int getLight(int idx) {
        return 0;
    }

    default int getForgeNormal(int idx) {
        return 0;
    }

}
