package cz.zelenikr.remotetouch.data.wrapper;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Parcelable wrapper of {@link Serializable} object.
 *
 * @author Roman Zelenik
 */
public class SerializableParcelWrapper implements Parcelable {

    public static final Creator<SerializableParcelWrapper> CREATOR = new Creator<SerializableParcelWrapper>() {
        @Override
        public SerializableParcelWrapper createFromParcel(Parcel in) {
            return new SerializableParcelWrapper(in);
        }

        @Override
        public SerializableParcelWrapper[] newArray(int size) {
            return new SerializableParcelWrapper[size];
        }
    };

    private Serializable content;

    private SerializableParcelWrapper(Parcel in) {
        content = in.readSerializable();
    }

    public SerializableParcelWrapper(Serializable content) {
        this.content = content;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(content);
    }

    public Serializable getContent() {
        return content;
    }

    public void setContent(Serializable content) {
        this.content = content;
    }
}
