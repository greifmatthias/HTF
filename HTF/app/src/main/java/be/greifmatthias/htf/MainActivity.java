package be.greifmatthias.htf;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

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
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;

import java.io.IOException;
import java.util.List;

import be.greifmatthias.htf.Helpers.ApiHelpers;
import be.greifmatthias.htf.Helpers.ThemeHelper;
import be.greifmatthias.htf.Models.Supply;
import be.greifmatthias.htf.Models.User;

public class MainActivity extends AppCompatActivity {

    // Auth
    private Auth0 _auth;

//    Api
    private ApiHelpers _apihelper;

//    Controls
    private RelativeLayout _rlLogin;
    private ListView _lvUsers;
    private RelativeLayout _rlOverlay;

    // map embedded in the map fragment
    private Map map = null;
    private MapFragment _frmMap;

//    Auth local
    private KeyguardManager _keymanager;
    private FingerprintManager _fingermanager;

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
        this._rlOverlay = findViewById(R.id.rlOverlay);

//        Init auth0
        _auth = new Auth0(this);



//        Setup fingerprint
        _keymanager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

//        Setup
        _rlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void showFingerbox(){
        final BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

                Log.d("local", "error");
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);

                Log.d("local", "help");
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                lock(false);

                final Handler h = new Handler();
                h.postDelayed(new Runnable()
                {
                    private long time = 0;

                    @Override
                    public void run()
                    {
                        time += 1000;
                        Log.d("TimerExample", "Going for... " + time);

                        lock(true);
                    }
                }, 10000); // 1 second delay (takes millis)

                Log.d("local", "succeed");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                Log.d("local", "failed");
            }
        };

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                callback.onAuthenticationFailed();
            }
        };

        new BiometricPrompt.Builder(this)
                .setTitle("Hi agent")
                .setSubtitle("Prove it is you")
                .setDescription("Provide us your finger")
                .setNegativeButton("dont", this.getMainExecutor(), listener)
                .build().authenticate(new CancellationSignal(), this.getMainExecutor(), callback);
    }

    private void setupMap(){
        this._frmMap.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = _frmMap.getMap();
                    // Set the map center to belgium
                    map.setCenter(new GeoCoordinate(50.5733473, 4.5534897, 8),
                            Map.Animation.NONE);
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);

                    _apihelper.getSupplies(new ApiHelpers.suppliescallback() {
                        @Override
                        public void onfinish(Supply[] supplies) {
                            for(int i = 0; i < supplies.length; i++){
                                double lat = supplies[i].lat;
                                double lng = supplies[i].lng;
                                GeoCoordinate coords = new GeoCoordinate(lat,lng);

                                Image img = new Image();
                                try {
                                    img.setImageResource(R.drawable.marker);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                MapMarker marker = new MapMarker(coords,img);
                                map.addMapObject(marker);

                            }
                        }
                    });


                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }
            }
        });
    }

    private void lock(boolean locked){
        if(locked){
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  _rlOverlay.setVisibility(View.VISIBLE);
              }
          });

            showFingerbox();
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _rlOverlay.setVisibility(View.GONE);
                }
            });
        }
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
                        //        Init map
                        setupMap();

                        lock(true);

                        showUsers();
                        showSupplies();
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

    private void showSupplies(){
        _apihelper.getSupplies(new ApiHelpers.suppliescallback() {
            @Override
            public void onfinish(Supply[] supplies) {
                for(int i = 0; i < supplies.length; i++){
                    Log.d("supplies", supplies[i].getName());
                }
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
