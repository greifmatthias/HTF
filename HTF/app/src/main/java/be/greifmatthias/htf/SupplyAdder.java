package be.greifmatthias.htf;

import android.app.Activity;
import android.os.Bundle;

import be.greifmatthias.htf.Helpers.ApiHelpers;
import be.greifmatthias.htf.Models.Supply;

public class SupplyAdder extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supply_adder);

        Supply supply = new Supply();
        supply.setAuthor("Gatsos");
        supply.setLat(51.1638813);
        supply.setLon(4.4271217);
        supply.setName("Gatsos medicinale verbanddoos");
        supply.setImage("geneimagejonge");

        ApiHelpers helpers = ApiHelpers.getInstance();
        helpers.addSupply(supply);
    }
}
