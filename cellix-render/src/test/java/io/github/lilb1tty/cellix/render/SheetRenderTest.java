package io.github.lilb1tty.cellix.render;

import io.github.lilb1tty.cellix.core.Cellix;
import io.github.lilb1tty.cellix.core.model.*;
import io.github.lilb1tty.cellix.core.model.style.CellStyle;
import io.github.lilb1tty.cellix.core.model.style.Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class SheetRenderTest {

    @TempDir
    Path tempDir;

    private static Worksheet sampleSheet() {
        Workbook wb = Cellix.create();
        Worksheet sheet = wb.sheet("Sheet1");
        sheet.cell("A1").setValue(new TextValue("Product"));
        sheet.cell("B1").setValue(new TextValue("Price"));
        sheet.cell("A2").setValue(new TextValue("Widget"));
        sheet.cell("B2").setValue(new NumberValue(29.99));
        sheet.cell("A3").setValue(new TextValue("Gadget"));
        sheet.cell("B3").setValue(new NumberValue(49.99));
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        sheet.cell("A1").setStyle(bold);
        sheet.cell("B1").setStyle(bold);
        return sheet;
    }

    @Test
    void toImage_returnsNonNullWithPositiveDimensions() {
        Worksheet sheet = sampleSheet();
        BufferedImage img = SheetRenderer.toImage(sheet, RenderOptions.defaults());
        assertThat(img).isNotNull();
        assertThat(img.getWidth()).isGreaterThan(0);
        assertThat(img.getHeight()).isGreaterThan(0);
    }

    @Test
    void toPng_writesValidPngFile() throws IOException {
        Worksheet sheet = sampleSheet();
        Path out = tempDir.resolve("sheet.png");
        SheetRenderer.toPng(sheet, out);

        assertThat(out).exists();
        assertThat(out.toFile().length()).isGreaterThan(0);
        // Verify it is actually readable as PNG
        BufferedImage read = ImageIO.read(out.toFile());
        assertThat(read).isNotNull();
        assertThat(read.getWidth()).isGreaterThan(0);
    }

    @Test
    void toJpeg_writesValidJpegFile() throws IOException {
        Worksheet sheet = sampleSheet();
        Path out = tempDir.resolve("sheet.jpg");
        SheetRenderer.toJpeg(sheet, out);

        assertThat(out).exists();
        assertThat(out.toFile().length()).isGreaterThan(0);
        BufferedImage read = ImageIO.read(out.toFile());
        assertThat(read).isNotNull();
    }

    @Test
    void renderOptions_customSize_affectsDimensions() {
        Worksheet sheet = sampleSheet();
        RenderOptions small  = new RenderOptions(48, 16, 9, 2);
        RenderOptions large  = new RenderOptions(120, 30, 14, 4);
        BufferedImage imgS = SheetRenderer.toImage(sheet, small);
        BufferedImage imgL = SheetRenderer.toImage(sheet, large);
        assertThat(imgL.getWidth()).isGreaterThan(imgS.getWidth());
        assertThat(imgL.getHeight()).isGreaterThan(imgS.getHeight());
    }

    @Test
    void emptySheet_rendersWithoutError() throws IOException {
        Workbook wb = Cellix.create();
        Worksheet sheet = wb.sheet(1);
        Path out = tempDir.resolve("empty.png");
        SheetRenderer.toPng(sheet, out);
        assertThat(out).exists();
    }

    @Test
    void cellText_formatsAllValueTypes() {
        Workbook wb = Cellix.create();
        Worksheet sheet = wb.sheet(1);

        Cell num  = sheet.cell("A1");  num.setValue(new NumberValue(42));
        Cell numF = sheet.cell("A2");  numF.setValue(new NumberValue(3.14));
        Cell bool = sheet.cell("A3");  bool.setValue(new BooleanValue(true));
        Cell err  = sheet.cell("A4");  err.setValue(new ErrorValue(ErrorType.DIV_BY_ZERO));
        Cell blank= sheet.cell("A5");  blank.setValue(new BlankValue());

        assertThat(SheetRenderer.cellText(num)).isEqualTo("42");
        assertThat(SheetRenderer.cellText(numF)).isEqualTo("3.14");
        assertThat(SheetRenderer.cellText(bool)).isEqualTo("TRUE");
        assertThat(SheetRenderer.cellText(err)).isEqualTo("#DIV/0!");
        assertThat(SheetRenderer.cellText(blank)).isEmpty();
    }
}
