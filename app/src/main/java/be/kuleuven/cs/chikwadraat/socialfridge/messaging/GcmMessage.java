package be.kuleuven.cs.chikwadraat.socialfridge.messaging;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import static be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants.ARG_HOST_USER_ID;
import static be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants.ARG_HOST_USER_NAME;
import static be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants.ARG_INVITEE_USER_ID;
import static be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants.ARG_PARTY_ID;
import static be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants.ARG_REASON_USER_ID;
import static be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants.ARG_REASON_USER_NAME;
import static be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants.ARG_TYPE;
import static be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants.ARG_UPDATE_REASON;

/**
 * Created by Mattias on 10/04/2014.
 */
public class GcmMessage implements Parcelable {

    private final MessageType type;

    private final Long partyID;

    private final PartyUpdateReason updateReason;
    private final String updateReasonUserID;
    private final String updateReasonUserName;

    private final String inviteeUserID;
    private final String hostUserID;
    private final String hostUserName;

    public GcmMessage(Bundle gcmMessageExtras) {
        type = MessageType.byName(gcmMessageExtras.getString(ARG_TYPE));

        String partyIDString = gcmMessageExtras.getString(ARG_PARTY_ID);
        partyID = partyIDString == null ? null : Long.parseLong(partyIDString);

        updateReason = PartyUpdateReason.byName(gcmMessageExtras.getString(ARG_UPDATE_REASON));
        updateReasonUserID = gcmMessageExtras.getString(ARG_REASON_USER_ID);
        updateReasonUserName = gcmMessageExtras.getString(ARG_REASON_USER_NAME);

        inviteeUserID = gcmMessageExtras.getString(ARG_INVITEE_USER_ID);
        hostUserID = gcmMessageExtras.getString(ARG_HOST_USER_ID);
        hostUserName = gcmMessageExtras.getString(ARG_HOST_USER_NAME);
    }

    public GcmMessage(Parcel in) {
        type = MessageType.byName(in.readString());

        partyID = (in.readByte() == 1) ? in.readLong() : null;

        // Updates
        updateReason = PartyUpdateReason.byName(in.readString());
        updateReasonUserID = in.readString();
        updateReasonUserName = in.readString();

        // Invites
        inviteeUserID = in.readString();
        hostUserID = in.readString();
        hostUserName = in.readString();
    }

    public MessageType getType() {
        return type;
    }

    public Long getPartyID() {
        return partyID;
    }

    public PartyUpdateReason getUpdateReason() {
        return updateReason;
    }

    public String getUpdateReasonUserID() {
        return updateReasonUserID;
    }

    public String getUpdateReasonUserName() {
        return updateReasonUserName;
    }

    public String getInviteeUserID() {
        return inviteeUserID;
    }

    public String getHostUserID() {
        return hostUserID;
    }

    public String getHostUserName() {
        return hostUserName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getType().getName());

        dest.writeByte((byte) (getPartyID() != null ? 1 : 0));
        if (getPartyID() != null) {
            dest.writeLong(getPartyID());
        }

        // Updates
        dest.writeString((getUpdateReason() != null) ? getUpdateReason().getName() : null);
        dest.writeString(getUpdateReasonUserID());
        dest.writeString(getUpdateReasonUserName());

        // Invites
        dest.writeString(getInviteeUserID());
        dest.writeString(getHostUserID());
        dest.writeString(getHostUserName());
    }

    public static final Parcelable.Creator<GcmMessage> CREATOR
            = new Parcelable.Creator<GcmMessage>() {
        public GcmMessage createFromParcel(Parcel in) {
            return new GcmMessage(in);
        }

        public GcmMessage[] newArray(int size) {
            return new GcmMessage[size];
        }
    };

}
