package io.excel4j.render;

public record RenderOptions(int cellWidth, int cellHeight, int fontSize, int padding) {

    public static RenderOptions defaults() {
        return new RenderOptions(96, 22, 11, 3);
    }
}
