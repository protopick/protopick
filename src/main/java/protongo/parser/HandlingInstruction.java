package protongo.parser;


public class HandlingInstruction {
    public String content;

    public HandlingInstruction(String givenContent) {
        content= givenContent;
    }

    void append(HandlingInstruction other) {
        content+= other.content;
    }
}
