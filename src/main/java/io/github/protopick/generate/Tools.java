package io.github.protopick.generate;

public class Tools {
    public static String asJsonString(String orig) {
        return "\"" +orig.replace( "\\", "\\\\" ).replace( "\"", "\\\"" )+ "\"";
    }
}
