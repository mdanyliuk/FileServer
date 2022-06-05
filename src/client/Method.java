package client;

import java.util.Arrays;

public enum Method {
    GET("1"),
    PUT("2"),
    DELETE("3"),
    EXIT("exit");

    public final String clientCode;

    private Method(String clientCode) {
        this.clientCode = clientCode;
    }

    public String getClientCode() {
        return clientCode;
    }

    public static Method getMethodByClientCode(String code) {
        return Arrays.stream(Method.values())
                .filter(method -> method.getClientCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
