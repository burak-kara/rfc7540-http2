package converter;

import java.util.BitSet;
import java.util.stream.IntStream;

public class Converter {
    // Long and BitSet converters-------------------------------------------------------
    public BitSet longToBitSet(long number) {
        BitSet set = new BitSet(8);
        int index = 0;
        while (number != 0L) {
            if (number % 2L != 0) set.set(index);
            ++index;
            number = number >>> 1;
        }
        return set;
    }

    public long bitSetToLong(BitSet set) {
        long sum = 0L;
        int bound = set.length();
        for (int i = 0; i < bound; i++) {
            long l = set.get(i) ? (1L << i) : 0L;
            sum += l;
        }
        return sum;
    }

    // String and BitSet converters ----------------------------------------------------
    public BitSet stringToBitSet(String str) {
        BitSet bitSet = new BitSet();
        for (int i = 0; i < str.length(); i++) {
            BitSet set = longToBitSet(str.charAt(i));
            bitSet = addBitSets(bitSet, set);
        }
        return bitSet;
    }

    public String bitSetToString(BitSet bitSet) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < bitSet.length(); i += 8)
            str.insert(0, bitSetToCharacter(bitSet.get(i, i + 8)));
        return str.toString();
    }

    private String bitSetToCharacter(BitSet bitSet) {
        long decimal = IntStream.range(0, 8).filter(bitSet::get).mapToLong(i -> (long) Math.pow(2, i)).sum();
        return Character.toString((char) decimal);
    }

    private BitSet addBitSets(BitSet last, BitSet first) {
        int len = first.length();
        int space = len % 8;
        if (len != 8 && len != 0) space = 8 - space;
        for (int i = 0; i < last.length(); i++)
            if (last.get(i)) first.set(len + space + i);
        return first;
    }

    // Hex and BitSet converters--------------------------------------------------------
    public BitSet hexToBitSet(String hex) {
        int i = Integer.parseInt(hex, 16);
        StringBuilder bin = new StringBuilder(Integer.toBinaryString(i));
        while (bin.length() < 8)
            bin.append("0");
        return binaryStringToBitSet(bin.toString());
    }

    private BitSet binaryStringToBitSet(String binaryArray) {
        BitSet bitSet = new BitSet(binaryArray.length());
        for (int i = 0; i < binaryArray.length(); i++)
            if (binaryArray.charAt(i) == '1') bitSet.set(i);
        return bitSet;
    }

    private String bitSetToBinaryString(BitSet bitSet) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bitSet.length(); i++) {
            if (bitSet.get(i)) builder.append("1");
            else builder.append("0");
        }
        return builder.toString();
    }

    public String bitSetToHex(BitSet bitSet) {
        return null;
    }

}
