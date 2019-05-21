import java.util.BitSet;

public class MainTest {
    public static void main(String[] args) {
        HTTP2 packet = new HTTP2();
        packet.createHeaderFrame();
        packet.createDataFrame();

        BitSet burak = (new Converter()).stringToBitSet("burak");
        System.out.println(burak);
        System.out.println((new Converter()).bitSetToLong(burak));

        System.out.println("--------------------------------------");

        BitSet x = (new Converter()).hexToBitSet("A");
        System.out.println(x);
        System.out.println((new Converter()).bitSetToLong(x));

        System.out.println("--------------------------------------");

        System.out.println((new Converter()).bitSetToString(burak));
    }
}
