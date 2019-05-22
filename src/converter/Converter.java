package converter;

public class Converter {
    // int and Binary String converter----------------------------------------
    public String intToBinaryString(int number, int length) {
        String str = Integer.toBinaryString(number);
        str = fixLength(str, length);
        return str;
    }

    private String fixLength(String str, int length) {
        StringBuilder strBuilder = new StringBuilder(str);
        while (strBuilder.length() < length)
            strBuilder.insert(0, "0");
        return strBuilder.toString();
    }

    // String and Binary String converter ----------------------------------------------------------
    public String stringToBinaryString(String str) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++)
            builder.append(intToBinaryString(str.charAt(i), 8));
            //builder.insert(0, intToBinaryString(str.charAt(i), 8));
        return builder.toString();
    }

    public String binaryStringToString(String str) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i += 8) {
            builder.append(getCharacter(str.substring(i, i+8)));
            //builder.insert(0, getCharacter(str.substring(i, i + 8)));
        }
        return builder.toString();
    }

    private String getCharacter(String str) {
        int dec = Integer.parseInt(str, 2);
        return Character.toString((char) dec);
    }
}
