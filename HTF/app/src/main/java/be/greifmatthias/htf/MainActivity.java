package be.greifmatthias.htf;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;

import java.io.IOException;

import be.greifmatthias.htf.Helpers.ApiHelpers;
import be.greifmatthias.htf.Helpers.DataHelper;
import be.greifmatthias.htf.Helpers.ThemeHelper;
import be.greifmatthias.htf.Models.Supply;
import be.greifmatthias.htf.Models.User;

public class MainActivity extends AppCompatActivity {

    private boolean isinlogin;
    private boolean isinlock;

    // Auth
    private Auth0 _auth;

//    Api
    private ApiHelpers _apihelper;

//    Controls
    private ImageView _ivMore;
    private RelativeLayout _rlLogin;
    private ListView _lvSupplies;
    private RelativeLayout _rlOverlay;

    private BiometricPrompt p;
    private BiometricPrompt.AuthenticationCallback callback;

    // map embedded in the map fragment
    private Map map = null;
    private MapFragment _frmMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Load theme
        ThemeHelper.setStatusbarWhite(getWindow(), true);

//        Init helpers
        DataHelper.getInstance(this);

//        Get controls
        this._rlLogin = findViewById(R.id.rlLogin);
        this._ivMore = findViewById(R.id.ivMore);
        this._lvSupplies = findViewById(R.id.lvSupplies);
        this._frmMap = (MapFragment) getFragmentManager().findFragmentById(R.id.frmMap);
        this._rlOverlay = findViewById(R.id.rlOverlay);

//        Init auth0
        _auth = new Auth0(this);

        isinlock = false;
        isinlogin = false;

//        Setup
        _rlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isinlogin = true;
                login();
            }
        });

        _ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SupplyAdder.class);
                startActivity(intent);
            }
        });

        _apihelper = ApiHelpers.getInstance();
        setupFingerbox();

    }

    @Override
    protected void onResume() {
        super.onResume();

        showSupplies();
        setupMap();

        showFingerbox();
    }

    private void setupFingerbox(){
        callback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

                setFingerHandler(1000);

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

                setFingerHandler(20000);

                Log.d("local", "succeed");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                setFingerHandler(1000);
            }
        };

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                callback.onAuthenticationFailed();
            }
        };

        p = new BiometricPrompt.Builder(this)
                .setTitle("Hi agent")
                .setSubtitle("Prove it is you")
                .setDescription("Provide us your finger")
                .setNegativeButton("dont", this.getMainExecutor(), listener)
                .build();
    }
    private void showFingerbox(){
        isinlock = true;
        p.authenticate(new CancellationSignal(), this.getMainExecutor(), callback);
    }

    private void setFingerHandler(int delay){
        final Handler h = new Handler();
        h.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                showFingerbox();
            }
        }, delay);
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
        isinlock = locked;

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

                        lock(false);

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
                    _ivMore.setVisibility(View.VISIBLE);
                } else {
                    _rlLogin.setVisibility(View.VISIBLE);
                    _ivMore.setVisibility(View.GONE);
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
                        //_lvUsers.setAdapter(new UsersAdapter(getBaseContext(), users));
                    }
                });
            }
        });
    }

    private void showSupplies(){
        _apihelper.getSupplies(new ApiHelpers.suppliescallback() {
            @Override
            public void onfinish(final Supply[] supplies) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _lvSupplies.setAdapter(new SuppliesAdapter(getBaseContext(), supplies));
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

    public class SuppliesAdapter extends BaseAdapter {

        private LayoutInflater myInflater;
        private Supply[] _supplies;

        public SuppliesAdapter(Context context, Supply[] supplies) {
            myInflater = LayoutInflater.from(context);
            this._supplies = supplies;
        }

        @Override
        public int getCount() {
            return _supplies.length;
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

            convertView = myInflater.inflate(R.layout.listitem_supply, null);
            holder = new ViewHolder();

            holder.tvName = convertView.findViewById(R.id.tvName);
            holder.ivIcon = convertView.findViewById(R.id.ivIcon);

            convertView.setTag(holder);

            holder.tvName.setText(_supplies[position].getName());

            try {
                byte[] decodedString = Base64.decode(_supplies[position].getImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivIcon.setImageBitmap(decodedByte);
            }catch (Exception ex){
                holder.ivIcon.setImageResource(R.drawable.marker);
            }

            return convertView;
        }

        class ViewHolder {
            TextView tvName;
            ImageView ivIcon ;

        }

    }
}
