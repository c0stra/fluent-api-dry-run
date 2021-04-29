package foundation.fluent.api.dry;

public interface User {

    Page<LoginForm> opens(String url);

    interface Page<T> {
        T andEnters();
    }

    interface LoginForm {
        LoginForm login(String username);
        LoginForm password(String password);
        void andSubmit();
    }

}
