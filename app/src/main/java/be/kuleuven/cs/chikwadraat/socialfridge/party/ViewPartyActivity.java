package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.api.client.repackaged.com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.DishItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;

/**
 * Activity to view a party.
 */
public class ViewPartyActivity extends BasePartyActivity implements View.OnClickListener {

    private static final String TAG = "ViewPartyActivity";

    private Button addCalendarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_view);

        addCalendarButton = (Button) findViewById(R.id.party_action_add_calendar);
        addCalendarButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.party_action_add_calendar:
                addCalendar(getParty(), getLoggedInUser());
                break;
        }
    }

    @Override
    public void onPartyLoaded(Party party, User user) {
        super.onPartyLoaded(party, user);
        addCalendarButton.setEnabled(party.isPlanned());
    }

    @Override
    public void onPartyUnloaded() {
        super.onPartyUnloaded();
        addCalendarButton.setEnabled(false);
    }

    private void addCalendar(Party party, User user) {
        if (party == null) return;

        // Build description with items to bring
        String description = "";
        PartyMember userPartner = party.getPartner(user);
        if (userPartner != null) {
            List<String> formattedBring = formatBringItems(userPartner.getBringItems());
            if (!formattedBring.isEmpty()) {
                description = getString(R.string.calendar_party_description,
                        Joiner.on('\n').join(formattedBring));
            }
        }

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("title", getString(R.string.calendar_party_title, party.getDish().getName()));
        intent.putExtra("description", description);
        intent.putExtra("eventLocation", getString(R.string.calendar_party_location, party.getHost().getUserName()));
        intent.putExtra("beginTime", party.getDate().getTime());
        intent.putExtra("endTime", party.getEndDate().getTime());
        intent.putExtra("allDay", false);
        startActivity(intent);
    }

    private List<String> formatBringItems(List<DishItem> bringItems) {
        List<String> formatted = new ArrayList<String>();
        for (DishItem bringItem : bringItems) {
            if (bringItem.getMeasure().getValue() > 0) {
                formatted.add(formatBringItem(bringItem));
            }
        }
        return formatted;
    }

    private String formatBringItem(DishItem bringItem) {
        return getString(R.string.calendar_party_bring_item_format,
                bringItem.getMeasure().toString(),
                bringItem.getIngredient().getName());
    }

}
