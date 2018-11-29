package be.greifmatthias.htf;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;

public class MainActivity extends AppCompatActivity {

    private Auth0 _auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _auth = new Auth0(this);

        login();
    }

    private void login() {
        WebAuthProvider.init(this._auth)
                .withScheme("demo")
                .withAudience(String.format("https://%s", getString(R.string.com_auth0_audiance)))
                .start(MainActivity.this, new AuthCallback() {
                    @Override
                    public void onFailure(@NonNull Dialog dialog) {
                        // Show error Dialog to user
                    }

                    @Override
                    public void onFailure(AuthenticationException exception) {
                        // Show error to user

                        Log.d("login", exception.getDescription());
                    }

                    @Override
                    public void onSuccess(@NonNull Credentials credentials) {
                        // Store credentials
                        // Navigate to your main activity

                        Log.d("login", "logedin");
                    }
                });
    }
}
