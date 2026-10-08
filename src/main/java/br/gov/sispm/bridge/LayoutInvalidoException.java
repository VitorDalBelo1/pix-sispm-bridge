package br.gov.sispm.bridge;

public class LayoutInvalidoException extends RuntimeException {
    public LayoutInvalidoException(String msg) { super(msg); }
    public LayoutInvalidoException(String msg, Throwable cause) { super(msg, cause); }
}
