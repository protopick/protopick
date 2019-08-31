package io.github.protopick.generate;

public class Tools {
    public static String asStringLiteral(String orig) {
        return "\"" +orig.replace( "\\", "\\\\" ).replace( "\"", "\\\"" )+ "\"";
    }
}
