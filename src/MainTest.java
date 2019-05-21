import converter.Converter;
import frame.Frame;
import frame.FramePayload;
import frame.HeaderFramePayload;
import http2.HTTP2;

import java.util.BitSet;

public class MainTest {
    public static void main(String[] args) {
        //testConverter();
        createPacket();
    }

    private static void testConverter() {
        BitSet burak = (new Converter()).stringToBitSet("burak");
        System.out.println(burak);
        System.out.println((new Converter()).bitSetToLong(burak));

        System.out.println("--------------------------------------");

        BitSet x = (new Converter()).hexToBitSet("A");
        System.out.println(x);
        System.out.println((new Converter()).bitSetToLong(x));

        System.out.println("--------------------------------------");

        System.out.println((new Converter()).bitSetToString(burak));
        System.out.println("--------------------------------------");
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

        FramePayload headerFramePayload = new HeaderFramePayload();

        headerFramePayload.setPadding(10);


        headerFrame.setFramePayload(headerFramePayload);
        headerFrame.setLength(headerFramePayload.getSize());

        //--------------------------------------------------
        Frame dataFrame = packet.getData();
    }
}
