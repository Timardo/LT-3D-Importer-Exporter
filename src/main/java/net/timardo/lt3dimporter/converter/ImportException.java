package net.timardo.lt3dimporter.converter;

public class ImportException extends Exception {
    private static final long serialVersionUID = 3968305724821591444L;
    
    private String msg;
    
    public ImportException(String message) {
        this.msg = message;
    }
    
    public String getReason() {
        return this.msg;
    }
}
