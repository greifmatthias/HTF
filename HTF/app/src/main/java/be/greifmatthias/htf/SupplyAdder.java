package be.greifmatthias.htf;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

import be.greifmatthias.htf.Helpers.ApiHelpers;
import be.greifmatthias.htf.Models.Supply;

public class SupplyAdder extends Activity {

    public static final int PICK_IMAGE = 1;

//    Controls
    private RelativeLayout _rlImageSelector;
    private RelativeLayout _rlSubmit;
    private TextView _tvName;
    private TextView _npLng;
    private TextView _npLat;

    private String image = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supply_adder);

//        Get controls
        this._rlImageSelector = findViewById(R.id.rlImageSelector);
        this._rlSubmit = findViewById(R.id.rlSubmit);
        this._tvName = findViewById(R.id.tvName);
        this._npLat = findViewById(R.id.etLat);
        this._npLng = findViewById(R.id.etLng);

        this._rlImageSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });

        this._rlSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Supply supply = new Supply();
                supply.setAuthor("");
                supply.setLat(Integer.parseInt(_npLat.getText().toString()));
                supply.setLon(Integer.parseInt(_npLng.getText().toString()));
                supply.setName(_tvName.getText().toString());
                supply.setImage(image);

                if(supply.getName() != null && supply.getImage() != null){
                    ApiHelpers helpers = ApiHelpers.getInstance();
                    helpers.addSupply(supply);

                    finish();
                }
            }
        });

//

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {
            if (resultCode != RESULT_OK) {
                return;
            }
            if (requestCode == 1) {
                final Bundle extras = data.getExtras();
                if (extras != null) {
                    //Get image
                    Bitmap bm = extras.getParcelable("data");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                    byte[] b = baos.toByteArray();

                    image = b.toString();
                }
            }
        }
    }
}
