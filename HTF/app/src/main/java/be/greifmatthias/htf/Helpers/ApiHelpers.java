package be.greifmatthias.htf.Helpers;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.greifmatthias.htf.Models.Supply;
import be.greifmatthias.htf.Models.User;

public class ApiHelpers {

    private String _token;
    private static ApiHelpers _THIS;

    private static final String DATA_USERS = "data_users";
    private static final String DATA_SUPPLIES = "data_supplies";

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private ApiHelpers(String token){
        this._token = token;
    }
    private ApiHelpers(){}

    public static ApiHelpers getInstance(String token){
        if(_THIS == null || _THIS._token == null){
            _THIS = new ApiHelpers(token);
        }

        return _THIS;
    }

    public static ApiHelpers getInstance(){
        if(_THIS == null){
            _THIS = new ApiHelpers();
        }

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
                call.onfinish(gson.fromJson(DataHelper.getInstance().read(DATA_SUPPLIES).get(0), User[].class));
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                if (response.isSuccessful()) {
                    //API call success

                    String resp = response.body().string();

                    List<String> savedata = new ArrayList<>();
                    savedata.add(resp);

                    DataHelper.getInstance().write(DATA_SUPPLIES, savedata);

                    User[] users = gson.fromJson(resp, User[].class);

                    call.onfinish(users);

                } else {
                    //API call failed. Check http error code and message
                    call.onfinish(gson.fromJson(DataHelper.getInstance().read(DATA_USERS).get(0), User[].class));
                }
            }
        });
    }

    public interface usercallback{
        void onfinish(User[] users);
    }

    public interface suppliescallback{
        void onfinish(Supply[] supplies);
    }

    public void getSupplies(final suppliescallback call){

        final Gson gson = new Gson();

        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .get()
                .url("https://htf2018.now.sh/supplies")
                .addHeader("Authorization", "Bearer " + this._token).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //show error
                call.onfinish(gson.fromJson(DataHelper.getInstance().read(DATA_SUPPLIES).get(0), Supply[].class));
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                if (response.isSuccessful()) {
                    //API call success

                    String resp = response.body().string();

                    List<String> savedata = new ArrayList<>();
                    savedata.add(resp);

                    DataHelper.getInstance().write(DATA_SUPPLIES, savedata);

                    Supply[] supplies = gson.fromJson(resp, Supply[].class);

                    call.onfinish(supplies);

                } else {
                    //API call failed. Check http error code and message

                    call.onfinish(gson.fromJson(DataHelper.getInstance().read(DATA_SUPPLIES).get(0), Supply[].class));
                }
            }
        });
    }

    public void addSupply(Supply supply){

        Gson gson = new Gson();

        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .post(RequestBody.create(MEDIA_TYPE_JSON, gson.toJson(supply)))
                .url("https://htf2018.now.sh/supplies")
                .addHeader("Authorization", "Bearer " + this._token).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {

            }
        });
    }
}
