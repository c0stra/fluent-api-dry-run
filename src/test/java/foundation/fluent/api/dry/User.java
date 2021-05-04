package foundation.fluent.api.dry;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface User extends Supplier<List<String>[][]> {

    Page<LoginForm> opens(String url);

    LoginPage opensLoginPage(String url);

    Consumer<List<Integer>[]> apply();

    List<?>[][] wc();

    interface LoginPage extends Page<LoginForm> {

    }

    interface Page<T> {
        <R> R fill(Function<Page<T>, R> function);
        Supplier<?> provider();
        T andEnters();
    }

    interface LoginForm {
        LoginForm login(String username);
        LoginForm password(String password);
        void andSubmit();
    }

}
