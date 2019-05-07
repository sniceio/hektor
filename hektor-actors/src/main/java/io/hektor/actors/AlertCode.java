package io.hektor.actors;


public enum AlertCode implements Alert {

    GENERIC_ERROR(1000,"{} Blah blah blah {}");

    private final int code;
    private final String msg;

    AlertCode(final int code, final String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getMessage() {
        return msg;
    }

    @Override
    public int getCode() {
        return code;
    }

}
