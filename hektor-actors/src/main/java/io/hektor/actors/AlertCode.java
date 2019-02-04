package io.hektor.actors;


public enum AlertCode {

    GENERIC_ERROR(1000,"{} Blah blah blah {}");

    private final int code;
    private final String msg;

    AlertCode(final int code, final String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMessage() {
        return msg;
    }

    public int getCode() {
        return code;
    }

}
