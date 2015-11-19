package nl.synappz.fhir.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;
import nl.synappz.fhir.R;

/**
 * A custom array adapter.
 */
public class SearchListAdapter extends ArrayAdapter<BundleEntry> {

    /**
     * Current context
     */
    protected Context mContext;

    private LayoutInflater mInflater;

    // -------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------

    public SearchListAdapter(Context context, List<BundleEntry> objects) {
        super(context, 0, objects);
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
    }


    // -------------------------------------------------------------
    //  getView()
    // -------------------------------------------------------------

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.searchlst_item_layout, parent, false);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();

            holder.name = (TextView) convertView.findViewById(R.id.name);

            convertView.setTag(holder);

        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        BundleEntry item = getItem(position);
        if (item != null) {
            Patient patient = (Patient)item.getResource();
            String familiyname = patient.getName().get(0).getFamily().get(0).getValueNotNull();
            String givename = null;
            if (patient.getName().get(0).getGiven().size()>0) {
                givename = patient.getName().get(0).getGiven().get(0).getValueNotNull();
            }
            String name = (givename!=null?givename+" ":"")+familiyname;

            holder.name.setText(name);
        }
        return convertView;
    }

    // -------------------------------------------------------------
    //  ViewHolder
    // -------------------------------------------------------------

    public Context getContext() {
        return mContext;
    }


    // -------------------------------------------------------------
    //  Getters and Setters
    // -------------------------------------------------------------

    private static class ViewHolder {

        public TextView name;

    }

}
