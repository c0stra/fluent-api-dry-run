package foundation.fluent.api.dry;

import java.util.function.Function;

public interface User {

    Page<LoginForm> opens(String url);

    LoginPage opensLoginPage(String url);

    interface LoginPage extends Page<LoginForm> {

    }

    interface Page<T> {
        <R> R fill(Function<Page<T>, R> function);
        T andEnters();
    }

    interface LoginForm {
        LoginForm login(String username);
        LoginForm password(String password);
        void andSubmit();
    }

}
