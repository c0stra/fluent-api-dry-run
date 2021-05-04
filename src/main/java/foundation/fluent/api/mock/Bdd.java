package foundation.fluent.api.mock;

public class Bdd {

    public static <T> Response<T> when(T mock) {
        return new Response<>();
    }

    public static class Response<T> {
        Response<T> thenReturn(T result, T... later) {
            return this;
        }
        Response<T> thenThrow(Throwable throwable) {
            return this;
        }
    }

}
