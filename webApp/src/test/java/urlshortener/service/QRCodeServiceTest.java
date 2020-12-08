package urlshortener.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import common.domain.ShortURL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import urlshortener.fixtures.ShortURLFixture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class QRCodeServiceTest {

    @Autowired
    QRCodeService qrCodeService = new QRCodeService();

    @Test
    void testUpdateQrURI() throws URISyntaxException {
        ShortURL shortURL = ShortURLFixture.exampleOrgUrl();
        shortURL.setUri(new URI("http://localhost:8080/random"));
        shortURL.setQrCode("qr/randomqrcodepath");
        // Call to method to test
        ShortURL shortURL1 = qrCodeService.updateQrURI(shortURL);
        // Check that qrcode is equals
        Assertions.assertEquals("http://localhost:8080/qr/randomqrcodepath", shortURL1.getQrCode(), "QR code path are different");
    }

    @Test
    void testGetQrCode() throws URISyntaxException, FormatException, ChecksumException, NotFoundException {
        ShortURL shortURL = ShortURLFixture.exampleOrgUrl();
        shortURL.setHash("random");
        shortURL.setUri(new URI("http://localhost:8080/random"));
        // Call to method to test
        String qrCodePath = qrCodeService.getQRCode(shortURL);
        shortURL.setQrCode(qrCodePath);
        Assertions.assertEquals("qr/random", shortURL.getQrCode(), "QR code paths are different");
        // Check created qrcode
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(shortURL.getQrCode() + ".png"));
        } catch (IOException e) {
            Assertions.fail("Ha saltado excepción en la lectura: \n" + Arrays.toString(e.getStackTrace()));
        }
        // Decode QR image
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Map<DecodeHintType, Object> hintMap = new HashMap<>();
        hintMap.put(DecodeHintType.PURE_BARCODE, true);
        QRCodeReader qrCodeReader = new QRCodeReader();
        String qrText = qrCodeReader.decode(bitmap, hintMap).getText();
        // Check results
        Assertions.assertEquals("http://localhost:8080/random", qrText, "QR encoded text are different");
    }

    @Test
    void testNoQrCode() {
        ShortURL shortURL = ShortURLFixture.exampleOrgUrl();
        Assertions.assertNull(shortURL.getQrCode(), "Initial value is not null");
        // Check method
        ShortURL shortURL1 = qrCodeService.noQrCode(shortURL);
        Assertions.assertEquals("", shortURL1.getQrCode(), "QR code paths are different");
    }

}