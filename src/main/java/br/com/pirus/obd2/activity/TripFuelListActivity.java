package br.com.pirus.obd2.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.pires.obd.reader.entity.EntityTripFuel;

import java.util.List;

import br.com.pirus.obd2.R;
import br.com.pirus.obd2.adapter.AdapterTripFuel;
import br.com.pirus.obd2.trips.TripFuel;

import static br.com.pirus.obd2.activity.ConfirmDialog.createDialog;

//import roboguice.activity.RoboActivity;

/**
 * Some code taken from https://github.com/wdkapps/FillUp
 */
public class TripFuelListActivity extends Activity implements ConfirmDialog.Listener {

    private List<EntityTripFuel> records;
    private List<EntityTripFuel> temp;

    private TripFuel tripfuel = null;
    private AdapterTripFuel adapter = null;

    /// the currently selected row from the list of records
    private int selectedRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_list);
        setupActionBar();

        ListView lv = findViewById(R.id.tripList);

        tripfuel = TripFuel.getInstance(this.getApplicationContext());
        temp = tripfuel.readAllRecords();


        records = temp;

/*        records = new ArrayList<>();

        long lastTime = 0;
        long lastPercent = 0;
        boolean abasteceu = false;


        for (EntityTripFuel trip : temp) {

            if (trip.getPercent() > lastPercent) {
                lastTime = trip.getTime();
                lastPercent = trip.getPercent();
                abasteceu = false;
            } else if (trip.getPercent() < lastPercent && !abasteceu) {
                abasteceu = true;

                System.out.println(" - - - - - - - - - - - - - - - - - -");
                System.out.println("lastTime: " + lastTime);
                System.out.println("lastInput: " + lastPercent);

                lastTime = trip.getTime();
                lastPercent = trip.getPercent();

                records.add(trip);
            }
        }*/


        adapter = new AdapterTripFuel(this, records);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
    }

    /**
     * Set up the {@link ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_trips_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement

        switch (item.getItemId()) {
            case android.R.id.home:
                // Do something here. This is the event fired when up button is pressed.
                finish();
                return true;

            case R.id.action_settings:
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        // create the menu
        getMenuInflater().inflate(R.menu.context_trip_list, menu);

        // get index of currently selected row
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedRow = (int) info.id;

        // get record that is currently selected
        EntityTripFuel record = records.get(selectedRow);
    }

/*    private void deleteTrip() {
        // get the record to delete from our list of records
        EntityTripFuel record = records.get(selectedRow);

        // attempt to remove the record from the log
        if (triplog.deleteTrip(record.getID())) {

            // remove the record from our list of records
            records.remove(selectedRow);

            // update the list view
            adapter.notifyDataSetChanged();
        } else {
            //Utilities.toast(this,getString(R.string.toast_delete_failed));
        }
    }*/

    public boolean onContextItemSelected(MenuItem item) {

        // get index of currently selected row
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        selectedRow = (int) info.id;

        switch (item.getItemId()) {
            case R.id.itemDelete:
                showDialog(ConfirmDialog.DIALOG_CONFIRM_DELETE_ID);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        return createDialog(id, this, this);
    }

    /**
     * DESCRIPTION:
     * Called when the user has selected a gasoline record to delete
     * from the log and has confirmed deletion.
     */
/*    protected void deleteRow() {

        // get the record to delete from our list of records
        EntityTripRecord record = records.get(selectedRow);

        // attempt to remove the record from the log
        if (triplog.deleteTrip(record.getID())) {
            records.remove(selectedRow);
            adapter.notifyDataSetChanged();
        } else {
            //Utilities.toast(this,getString(R.string.toast_delete_failed));
        }
    }*/
    @Override
    public void onConfirmationDialogResponse(int id, boolean confirmed) {
        removeDialog(id);

        if (!confirmed) return;

        switch (id) {
            case ConfirmDialog.DIALOG_CONFIRM_DELETE_ID:
                //deleteRow();
                break;

            default:
                //Utilities.toast(this,"Invalid dialog id.");
        }
    }
}
