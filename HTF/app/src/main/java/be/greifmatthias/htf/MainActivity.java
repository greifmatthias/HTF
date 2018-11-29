package be.greifmatthias.htf;

import android.app.Dialog;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;

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
    private RelativeLayout _rlLogin;
    private ListView _lvUsers;

    // map embedded in the map fragment
    private Map map = null;
    private MapFragment _frmMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Load theme
        ThemeHelper.setStatusbarWhite(getWindow(), true);

//        Get controls
        this._rlLogin = findViewById(R.id.rlLogin);
        this._lvUsers = findViewById(R.id.lvUsers);
        this._frmMap = (MapFragment) getFragmentManager().findFragmentById(R.id.frmMap);

//        Init auth0
        _auth = new Auth0(this);

//        Init map
        setupMap();

//        Setup
        _rlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void setupMap(){
        this._frmMap.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = _frmMap.getMap();
                    // Set the map center to the Vancouver region (no animation)
                    map.setCenter(new GeoCoordinate(49.196261, -123.004773, 0.0),
                            Map.Animation.NONE);
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);
                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }
            }
        });
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
                    }

                    @Override
                    public void onSuccess(@NonNull Credentials credentials) {
                        _apihelper = ApiHelpers.getInstance(credentials.getAccessToken());

                        checkLogin(true);

                        showUsers();
                    }
                });
    }

    private void checkLogin(final boolean logedin){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (logedin) {
                    _rlLogin.setVisibility(View.GONE);
                } else {
                    _rlLogin.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showUsers(){
        _apihelper.getUsers(new ApiHelpers.usercallback() {
            @Override
            public void onfinish(final User[] users) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _lvUsers.setAdapter(new UsersAdapter(getBaseContext(), users));
                    }
                });
            }
        });
    }

    public class UsersAdapter extends BaseAdapter {

        private LayoutInflater myInflater;
        private User[] _users;

        public UsersAdapter(Context context, User[] users) {
            myInflater = LayoutInflater.from(context);
            this._users = users;
        }

        @Override
        public int getCount() {
            return _users.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            convertView = myInflater.inflate(R.layout.listitem_user, null);
            holder = new ViewHolder();

            holder.tvId = convertView.findViewById(R.id.tvId);
            holder.tvMail = convertView.findViewById(R.id.tvMail);

            convertView.setTag(holder);

            holder.tvId.setText(_users[position].getUser_id());
            holder.tvMail.setText(_users[position].getEmail());

            return convertView;
        }

        class ViewHolder {
            TextView tvId;
            TextView tvMail ;

        }

    }
}
