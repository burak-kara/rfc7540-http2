import converter.Converter;
import frame.*;
import http2.HTTP2;

public class MainTest {
    public static void main(String[] args) {
        testConverter();
        //createPacket();
    }

    private static void testConverter() {
        Converter converter = new Converter();
        System.out.println(converter.intToBinaryString(32,10));
        String burak = converter.stringToBinaryString("burak\nkaraonur\nkirman");
        System.out.println(burak);
        System.out.println(converter.binaryStringToString(burak));
        System.out.println();
    }

    private static void createPacket() {
        HTTP2 packet = new HTTP2();
        packet.createHeaderFrame();
        packet.createDataFrame();

        // -----------------------------------------------
        Frame headerFrame = packet.getHeader();
        headerFrame.setType(1);
        headerFrame.setFlags(10);
        headerFrame.setR(1);
        headerFrame.setStreamIdentifier(100);

        HeaderFramePayload headerPayload = new HeaderFramePayload();
        headerPayload.setPadLength(10);
        headerPayload.setE(1);
        headerPayload.setDependency(10);
        headerPayload.setWeight(20);
        headerPayload.setHeaders(getHeader());
        headerPayload.setPadding(10);

        headerFrame.setFramePayload(headerPayload);
        headerFrame.setLength(headerPayload.getSize());

        //--------------------------------------------------
    }

    private static String getHeader() {
        return "GET /home.html HTTP/2\n" +
                "Host: developer.mozilla.org\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:50.0) Gecko/20100101 Firefox/50.0\n" +
                "Accept-Language: en-US,en;q=0.5\n" +
                "Connection: keep-alive\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n";
    }
}
