package be.greifmatthias.htf.Helpers;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import be.greifmatthias.htf.Models.User;

public class ApiHelpers {

    private String _token;
    private static ApiHelpers _THIS;

    private ApiHelpers(String token){
        this._token = token;
    }

    public static ApiHelpers getInstance(String token){
        if(_THIS == null){
            _THIS = new ApiHelpers(token);
        }

        return _THIS;
    }

    public static ApiHelpers getInstance(){
        return _THIS;
    }

    public void getUsers(final usercallback call){

        final Gson gson = new Gson();

        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .get()
                .url("https://htf2018.now.sh/users")
                .addHeader("Authorization", "Bearer " + this._token).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //show error
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                if (response.isSuccessful()) {
                    //API call success

                    String resp = response.body().string();

                    Log.d("getuser", resp);
                    User[] users = gson.fromJson(resp, User[].class);

                    call.onfinish(users);

                } else {
                    //API call failed. Check http error code and message
                    Log.d("getusers", "failed");
                }
            }
        });
    }

    public interface usercallback{
        void onfinish(User[] users);
    }

    private void getSupplies(){
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .get()
                .url("https://htf2018.now.sh/users")
                .addHeader("Authorization", "Bearer " + this._token).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //show error
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                if (response.isSuccessful()) {
                    //API call success

                    Log.d("getuser", response.body().string());
                } else {
                    //API call failed. Check http error code and message
                    Log.d("getusers", "failed");
                }
            }
        });
    }
}
