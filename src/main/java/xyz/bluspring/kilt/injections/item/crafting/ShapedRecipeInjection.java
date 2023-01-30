package xyz.bluspring.kilt.injections.item.crafting;

public interface ShapedRecipeInjection {
    int MAX_WIDTH = 3;
    int MAX_HEIGHT = 3;

    static void setCraftingSize(int width, int height) {
        // i don't think this can be done. oops.
        /*if (MAX_WIDTH < width)
            MAX_WIDTH = width;

        if (MAX_HEIGHT < height)
            MAX_HEIGHT = height;*/
    }
}
