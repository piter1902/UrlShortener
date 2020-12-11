package urlshortener.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import urlshortener.domain.ShortURL;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class QRCodeService {

    private final Logger log = LoggerFactory.getLogger(QRCodeService.class);


    /**
     * Private method that returns QR code path for [su]. qr directory must exist in project's root.
     * <p>
     * Sources:
     * https://www.baeldung.com/java-generating-barcodes-qr-codes
     * https://stackoverflow.com/questions/7178937/java-bufferedimage-to-png-format-base64-string/25109418
     *
     * @param su shortUrl object to encode
     * @return Path of qr image file that contains uri to redirect
     * @throws WriterException iff QR encoder fails
     * @throws IOException     iff ByteArrayOutputStream fails
     */
    private String generateQRCode(ShortURL su) throws WriterException, IOException {
        // Create QR path
        String qrFilePath = "qr/" + su.getHash();
        processQrAsync(su, qrFilePath);
        return qrFilePath;
    }

    /**
     * Creates and writes QR code asynchronously and stores it in [qrFilePath] as PNG file
     *
     * @param su         to convert to QR code
     * @param qrFilePath to save QR code
     * @throws WriterException iff QR encoder fails
     * @throws IOException     iff ByteArrayOutputStream fails
     */
    @Async
    private void processQrAsync(ShortURL su, String qrFilePath) throws WriterException, IOException {
        log.info(String.format("Processing QR code for %s asynchronously\n", su.getTarget()));
        // Process QR asynchronously
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(su.getUri().toASCIIString(), BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        // Save QR to /qr directory
        ImageIO.write(bufferedImage, "png", new File(qrFilePath + ".png"));
        log.info("End of async processing");
    }

    /**
     * Method that returns an updated [su] object
     * to with su.qrCode using su.uri(scheme, host, port)
     *
     * @param su object to update.
     * @return su object with qrcode updated
     */
    public ShortURL updateQrURI(ShortURL su) {
        // Add URI prefix to qrCode path
        URI uri = su.getUri();
        try {
            su.setQrCode(new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(),
                    "/" + su.getQrCode(), null, null).toASCIIString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return su;
    }

    /**
     * Method that returns QR code path for [su]. qr directory must exist in project's root.
     * <p>
     * Sources:
     * https://www.baeldung.com/java-generating-barcodes-qr-codes
     * https://stackoverflow.com/questions/7178937/java-bufferedimage-to-png-format-base64-string/25109418
     *
     * @param su shortUrl object to encode
     * @return Path of qr image file that contains uri to redirect or empty string if error occurred.
     */
    public String getQRCode(ShortURL su) {
        String qrCode = "";
        try {
            qrCode = generateQRCode(su);
        } catch (WriterException | IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return qrCode;
    }

    /**
     * Returns [su] object with qrCode field value equals to empty string ("")
     *
     * @param su object to modify
     * @return su object without qrcode
     */
    public ShortURL noQrCode(ShortURL su) {
        su.setQrCode("");
        return su;
    }

    /**
     * Returns byte array containing qr code image.
     *
     * @param hash to obtain qr code
     * @return qr image byte array
     * @throws IOException iff ImageIO.write from QR code image to byteOutputStream fails.
     */
    public byte[] getQrByteArray(String hash) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File("qr/" + hash + ".png"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
