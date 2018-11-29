package be.greifmatthias.htf;

import android.app.Dialog;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.management.ManagementException;
import com.auth0.android.management.UsersAPIClient;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

import be.greifmatthias.htf.Helpers.ApiHelpers;
import be.greifmatthias.htf.Helpers.ThemeHelper;
import be.greifmatthias.htf.Models.User;

public class MainActivity extends AppCompatActivity {

    // Auth
    private Auth0 _auth;

//    Api
    private ApiHelpers _apihelper;

//    Controls
    private RecyclerView _rvUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Load theme
        ThemeHelper.setStatusbarWhite(getWindow(), true);

//        Get controls
        this._rvUsers = findViewById(R.id.rvUsers);

//        Init auth0
        _auth = new Auth0(this);

//        Force to login
        login();
    }

    private void login() {
        WebAuthProvider.init(this._auth).withScheme("demo").withAudience(String.format("https://%s", getString(R.string.com_auth0_audiance)))
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
                        _apihelper = ApiHelpers.getInstance(credentials.getAccessToken());
                        showUsers();
                    }
                });
    }

    private void showUsers(){
        this._rvUsers.setAdapter(new UsersAdapter(_apihelper.getUsers()));
    }

    public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
        private List<User> _users;

        public UsersAdapter(List<User> users) {
            this._users = users;
        }

        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_user, parent, false);
            return new ViewHolder(v);
        }

        @Override public void onBindViewHolder(ViewHolder holder, int position) {
            // Get view
            User item = this._users.get(position);

            // Set content
            holder.tvId.setText(item.getId());
            holder.tvMail.setText(item.getEmail());

            holder.itemView.setTag(item);
        }

        @Override public int getItemCount() {
            return this._users.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tvId;
            public TextView tvMail;

            public ViewHolder(View itemView) {
                super(itemView);

                this.tvId = itemView.findViewById(R.id.tvId);
                this.tvMail = itemView.findViewById(R.id.tvMail);
            }
        }
    }
}
