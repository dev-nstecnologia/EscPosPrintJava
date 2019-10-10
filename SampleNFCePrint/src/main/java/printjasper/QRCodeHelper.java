package printjasper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

public class QRCodeHelper {
    public QRCodeHelper() {}

    public static void generateQRCodeToPNGFile(String content, Path pathTarget) throws IOException {
        File qrCodeFile = QRCode.from(content).withSize(300, 300).to(ImageType.PNG).file();
        Files.move(Paths.get(qrCodeFile.getAbsolutePath()), pathTarget);
    }

    public static ByteArrayInputStream generateQRCodeToPNGStream(String content) {
        ByteArrayOutputStream out = QRCode.from(content).withSize(500, 500).to(ImageType.PNG).stream();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
