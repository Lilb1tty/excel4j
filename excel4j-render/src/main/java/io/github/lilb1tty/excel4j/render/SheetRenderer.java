package io.github.lilb1tty.excel4j.render;

import io.github.lilb1tty.excel4j.core.model.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import javax.imageio.ImageIO;

public final class SheetRenderer {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    private SheetRenderer() {}

    public static BufferedImage toImage(Worksheet sheet, RenderOptions opts) {
        Map<CellRef, Cell> cells = sheet.cells();

        int maxRow = 1, maxCol = 1;
        for (CellRef ref : cells.keySet()) {
            maxRow = Math.max(maxRow, ref.row());
            maxCol = Math.max(maxCol, ref.col());
        }

        int headerW = 40;
        int headerH = 20;
        int imgW = headerW + maxCol * opts.cellWidth();
        int imgH = headerH + maxRow * opts.cellHeight();

        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imgW, imgH);

        Font baseFont   = new Font("SansSerif", Font.PLAIN, opts.fontSize());
        Font boldFont   = new Font("SansSerif", Font.BOLD,  opts.fontSize());
        Font headerFont = new Font("SansSerif", Font.BOLD,  opts.fontSize() - 1);

        // Column header bar
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, imgW, headerH);
        g.setColor(new Color(180, 180, 180));
        g.drawLine(0, headerH - 1, imgW, headerH - 1);

        g.setFont(headerFont);
        FontMetrics hfm = g.getFontMetrics();
        g.setColor(new Color(80, 80, 80));
        for (int col = 1; col <= maxCol; col++) {
            String label = colLetters(col);
            int x = headerW + (col - 1) * opts.cellWidth();
            g.drawString(label, x + (opts.cellWidth() - hfm.stringWidth(label)) / 2, headerH - 4);
            g.setColor(new Color(200, 200, 200));
            g.drawLine(x + opts.cellWidth() - 1, 0, x + opts.cellWidth() - 1, headerH);
            g.setColor(new Color(80, 80, 80));
        }

        // Row header bar
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, headerH, headerW, imgH - headerH);
        g.setColor(new Color(180, 180, 180));
        g.drawLine(headerW - 1, 0, headerW - 1, imgH);
        g.setFont(headerFont);
        FontMetrics rfm = g.getFontMetrics();
        g.setColor(new Color(80, 80, 80));
        for (int row = 1; row <= maxRow; row++) {
            String label = String.valueOf(row);
            int y = headerH + (row - 1) * opts.cellHeight();
            g.drawString(label, headerW - rfm.stringWidth(label) - 4,
                         y + (opts.cellHeight() + rfm.getAscent() - rfm.getDescent()) / 2);
            g.setColor(new Color(200, 200, 200));
            g.drawLine(0, y + opts.cellHeight() - 1, headerW, y + opts.cellHeight() - 1);
            g.setColor(new Color(80, 80, 80));
        }

        // Cell grid
        g.setColor(new Color(220, 220, 220));
        for (int col = 1; col <= maxCol; col++) {
            int x = headerW + col * opts.cellWidth() - 1;
            g.drawLine(x, headerH, x, imgH);
        }
        for (int row = 1; row <= maxRow; row++) {
            int y = headerH + row * opts.cellHeight() - 1;
            g.drawLine(headerW, y, imgW, y);
        }

        // Cell values
        for (Map.Entry<CellRef, Cell> entry : cells.entrySet()) {
            CellRef ref   = entry.getKey();
            Cell    cell  = entry.getValue();
            String  text  = cellText(cell);
            if (text.isEmpty()) continue;

            boolean isBold = cell.getStyle().font().isBold();
            g.setFont(isBold ? boldFont : baseFont);
            FontMetrics fm = g.getFontMetrics();

            int cellX = headerW + (ref.col() - 1) * opts.cellWidth();
            int cellY = headerH + (ref.row() - 1) * opts.cellHeight();
            int textX = cellX + opts.padding();
            int textY = cellY + (opts.cellHeight() + fm.getAscent() - fm.getDescent()) / 2;

            g.setColor(new Color(30, 30, 30));
            Shape clip = g.getClip();
            g.clipRect(cellX + 1, cellY + 1, opts.cellWidth() - 2, opts.cellHeight() - 2);
            g.drawString(text, textX, textY);
            g.setClip(clip);
        }

        g.dispose();
        return img;
    }

    public static void toPng(Worksheet sheet, Path path) throws IOException {
        toPng(sheet, path, RenderOptions.defaults());
    }

    public static void toPng(Worksheet sheet, Path path, RenderOptions opts) throws IOException {
        ImageIO.write(toImage(sheet, opts), "png", path.toFile());
    }

    public static void toJpeg(Worksheet sheet, Path path) throws IOException {
        toJpeg(sheet, path, RenderOptions.defaults());
    }

    public static void toJpeg(Worksheet sheet, Path path, RenderOptions opts) throws IOException {
        BufferedImage src = toImage(sheet, opts);
        // JPEG does not support alpha — flatten to RGB
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();
        ImageIO.write(rgb, "jpeg", path.toFile());
    }

    static String cellText(Cell cell) {
        return switch (cell.getValue()) {
            case TextValue(var s)       -> s;
            case NumberValue(var d)     -> formatNumber(d);
            case BooleanValue(var b)    -> b ? "TRUE" : "FALSE";
            case ErrorValue(var t)      -> t.toExcelString();
            case DateValue(var d)       -> d.toString();
            case DateTimeValue(var dt)  -> dt.toString();
            case BlankValue()           -> "";
            case RangeValue(var r, var v) -> "";
        };
    }

    private static String formatNumber(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    static String colLetters(int col) {
        StringBuilder sb = new StringBuilder();
        while (col > 0) {
            col--;
            sb.insert(0, (char) ('A' + col % 26));
            col /= 26;
        }
        return sb.toString();
    }
}
