package io.github.lilb1tty.cellix.render;

import io.github.lilb1tty.cellix.core.model.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PdfRenderer {

    private static final int PAGE_W = 595; // A4 points at 72 dpi
    private static final int PAGE_H = 842;

    private PdfRenderer() {}

    public static void toPdf(Workbook workbook, Path path) throws IOException {
        toPdf(workbook, path, RenderOptions.defaults());
    }

    public static void toPdf(Workbook workbook, Path path, RenderOptions opts) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        for (Worksheet sheet : workbook.sheets()) {
            images.add(SheetRenderer.toImage(sheet, opts));
        }
        try (OutputStream out = Files.newOutputStream(path)) {
            writePdf(images, out);
        }
    }

    public static void toPdf(Worksheet sheet, Path path) throws IOException {
        toPdf(sheet, path, RenderOptions.defaults());
    }

    public static void toPdf(Worksheet sheet, Path path, RenderOptions opts) throws IOException {
        BufferedImage img = SheetRenderer.toImage(sheet, opts);
        try (OutputStream out = Files.newOutputStream(path)) {
            writePdf(List.of(img), out);
        }
    }

    // PDF object layout for n pages:
    //  1       = catalog
    //  2       = pages
    //  3..n+2  = page objects
    //  n+3..2n+2 = image XObjects
    //  2n+3..3n+2 = content streams
    private static void writePdf(List<BufferedImage> images, OutputStream sink) throws IOException {
        int n = images.size();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        List<long[]> offsets = new ArrayList<>(); // [objId, byteOffset]

        write(buf, "%PDF-1.4\n");
        buf.write(new byte[]{0x25, (byte) 0xe2, (byte) 0xe3, (byte) 0xcf, (byte) 0xd3, 0x0a});

        // Object 1 — catalog
        offsets.add(new long[]{1, buf.size()});
        writeObj(buf, 1, "<< /Type /Catalog /Pages 2 0 R >>");

        // Object 2 — pages
        offsets.add(new long[]{2, buf.size()});
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) kids.append(' ');
            kids.append(3 + i).append(" 0 R");
        }
        writeObj(buf, 2, "<< /Type /Pages /Kids [" + kids + "] /Count " + n + " >>");

        // Pre-compute RGB bytes for all images
        List<byte[]> rgbList = new ArrayList<>();
        for (BufferedImage img : images) {
            rgbList.add(toRgb(img));
        }

        // Page objects
        for (int i = 0; i < n; i++) {
            int imgId     = 3 + n + i;
            int contentId = 3 + 2 * n + i;
            offsets.add(new long[]{3 + i, buf.size()});
            writeObj(buf, 3 + i,
                "<< /Type /Page /Parent 2 0 R"
                + " /MediaBox [0 0 " + PAGE_W + " " + PAGE_H + "]"
                + " /Contents " + contentId + " 0 R"
                + " /Resources << /XObject << /Im" + (i + 1) + " " + imgId + " 0 R >> >>"
                + " >>");
        }

        // Image XObjects
        for (int i = 0; i < n; i++) {
            BufferedImage img = images.get(i);
            byte[] rgb = rgbList.get(i);
            offsets.add(new long[]{3 + n + i, buf.size()});
            writeStreamObj(buf, 3 + n + i,
                "<< /Type /XObject /Subtype /Image"
                + " /Width " + img.getWidth() + " /Height " + img.getHeight()
                + " /ColorSpace /DeviceRGB /BitsPerComponent 8"
                + " /Length " + rgb.length + " >>",
                rgb);
        }

        // Content streams
        for (int i = 0; i < n; i++) {
            BufferedImage img = images.get(i);
            double scale = Math.min((double) PAGE_W / img.getWidth(),
                                    (double) PAGE_H / img.getHeight());
            int drawW = (int) (img.getWidth() * scale);
            int drawH = (int) (img.getHeight() * scale);
            int offX  = (PAGE_W - drawW) / 2;
            int offY  = (PAGE_H - drawH) / 2;
            String cs = "q\n" + drawW + " 0 0 " + drawH + " " + offX + " " + offY + " cm\n"
                      + "/Im" + (i + 1) + " Do\nQ\n";
            byte[] csBytes = cs.getBytes(StandardCharsets.US_ASCII);
            offsets.add(new long[]{3 + 2 * n + i, buf.size()});
            writeStreamObj(buf, 3 + 2 * n + i,
                "<< /Length " + csBytes.length + " >>", csBytes);
        }

        // xref table
        int totalObjs = 2 + 3 * n; // catalog + pages + n*(page+image+content)
        long xrefOffset = buf.size();
        write(buf, "xref\n");
        write(buf, "0 " + (totalObjs + 1) + "\n");
        write(buf, "0000000000 65535 f\r\n"); // free object 0

        // Sort offsets by object ID to emit xref entries in order
        offsets.sort((a, b) -> Long.compare(a[0], b[0]));
        for (long[] entry : offsets) {
            write(buf, String.format("%010d 00000 n\r\n", entry[1]));
        }

        // Trailer
        write(buf, "trailer\n");
        write(buf, "<< /Size " + (totalObjs + 1) + " /Root 1 0 R >>\n");
        write(buf, "startxref\n");
        write(buf, xrefOffset + "\n");
        write(buf, "%%EOF\n");

        sink.write(buf.toByteArray());
    }

    private static void writeObj(ByteArrayOutputStream buf, int id, String dict) throws IOException {
        write(buf, id + " 0 obj\n" + dict + "\nendobj\n\n");
    }

    private static void writeStreamObj(ByteArrayOutputStream buf, int id,
            String dict, byte[] data) throws IOException {
        write(buf, id + " 0 obj\n" + dict + "\nstream\n");
        buf.write(data);
        write(buf, "\nendstream\nendobj\n\n");
    }

    private static void write(ByteArrayOutputStream buf, String s) throws IOException {
        buf.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static byte[] toRgb(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        byte[] data = new byte[w * h * 3];
        int idx = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                data[idx++] = (byte) ((rgb >> 16) & 0xFF);
                data[idx++] = (byte) ((rgb >> 8) & 0xFF);
                data[idx++] = (byte) (rgb & 0xFF);
            }
        }
        return data;
    }
}
