package com.github.pires.obd.reader.adapter;

import android.app.Activity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.pires.obd.reader.R;
import com.github.pires.obd.reader.entity.EntityTripFuel;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterTripFuel extends ArrayAdapter<EntityTripFuel> {

    /// the Android Activity owning the ListView
    private final Activity activity;

    /// a list of trip records for display
    private final List<EntityTripFuel> records;

    /**
     * DESCRIPTION:
     * Constructs an instance of TripListAdapter.
     *
     * @param activity - the Android Activity instance that owns the ListView.
     * @param records  - the List of TripRecord instances for display in the ListView.
     */
    public AdapterTripFuel(Activity activity, List<EntityTripFuel> records) {
        super(activity, R.layout.row_trip_list, records);
        this.activity = activity;
        this.records = records;
    }

    /**
     * DESCRIPTION:
     * Constructs and populates a View for display of the TripRecord at the index
     * of the List specified by the position parameter.
     *
     * @see ArrayAdapter#getView(int, View, ViewGroup)
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // create a view for the row if it doesn't already exist
        if (view == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            view = inflater.inflate(R.layout.row_trip_list, null);
        }

        // get widgets from the view
        TextView startDate = view.findViewById(R.id.startDate);
        TextView columnDuration = view.findViewById(R.id.columnDuration);
        TextView rowEngine = view.findViewById(R.id.rowEngine);
        //TextView rowOther = view.findViewById(R.id.rowOther);

        // populate row widgets from record data
        EntityTripFuel record = records.get(position);

        // date
        rowEngine.setText(String.format("%.2f", record.getInputFuel()) + "%");
        //columnDuration.setText(getDate(record.getTimeStamp()));
        startDate.setText(getDate(record.getTimeStamp()));

        return view;
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        return DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
    }

/*    private String calcDiffTime(Date start, Date end) {
        long diff = end.getTime() - start.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        StringBuffer res = new StringBuffer();

        if (diffDays > 0)
            res.append(diffDays + "d");

        if (diffHours > 0) {
            if (res.length() > 0) {
                res.append(" ");
            }
            res.append(diffHours + "h");
        }

        if (diffMinutes > 0) {
            if (res.length() > 0) {
                res.append(" ");
            }
            res.append(diffMinutes + "m");
        }

        if (diffSeconds > 0) {
            if (res.length() > 0) {
                res.append(" ");
            }

            res.append(diffSeconds + "s");
        }
        return res.toString();
    }*/

    /**
     * DESCRIPTION:
     * Called by parent when the underlying data set changes.
     *
     * @see ArrayAdapter#notifyDataSetChanged()
     */
    @Override
    public void notifyDataSetChanged() {

        // configuration may have changed - get current settings
        //todo
        //getSettings();

        super.notifyDataSetChanged();
    }
}