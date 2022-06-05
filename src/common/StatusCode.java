package common;

public enum StatusCode {
    OK("200"),
    FORBIDDEN("403"),
    NOT_FOUND("404");

    public final String code;

    private StatusCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code;
    }
}
