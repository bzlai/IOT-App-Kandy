
package com.kandy.starter.utils;

import com.genband.kandy.api.services.chats.KandyChatSettings;
import com.kandy.starter.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

public class MediaSizePicker extends DialogPreference {

	private int mDefaultValue;
	private int mMaxValue;
	private int mCurrentValue = -1, mNewValue;
	private NumberPicker mNumberPicker;


	public MediaSizePicker(Context context, AttributeSet attrs) {
		super(context, attrs);

		setDialogLayoutResource(R.layout.numberpicker_dialog);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

		setDialogIcon(null);

		mDefaultValue = KandyChatSettings.MESSAGE_MEDIA_MAX_SIZE;
		mMaxValue = KandyChatSettings.MESSAGE_MEDIA_MAX_SIZE;
	}

	public int getValue() {
		return mCurrentValue;
	}
	
	@Override
	protected View onCreateDialogView() {
		int max = mMaxValue;
		int min = 0;

		LayoutInflater inflater =
				(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.numberpicker_dialog, null, false);

		mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);

		// Initialize state
		mNumberPicker.setMaxValue(max);
		mNumberPicker.setMinValue(min);
		mNumberPicker.setValue(getPersistedInt(mDefaultValue));
		mNumberPicker.setWrapSelectorWheel(false);

		return view;
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, mDefaultValue);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			// Restore existing state
			mCurrentValue = this.getPersistedInt(mDefaultValue);
		} else {
			// Set default state from the XML attribute
			mCurrentValue = (Integer) defaultValue;
			persistInt(mCurrentValue);
			
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// When the user selects "OK", persist the new value
		if (positiveResult) {
			mNewValue = mNumberPicker.getValue();
			mCurrentValue = mNewValue;
			persistInt(mNewValue);
		}
	}

		@Override
	protected Parcelable onSaveInstanceState() {
	    final Parcelable superState = super.onSaveInstanceState();
	    // Check whether this Preference is persistent (continually saved)
	    if (isPersistent()) {
	        // No need to save instance state since it's persistent,
	        // use superclass state
	        return superState;
	    }

	    // Create instance of custom BaseSavedState
	    final SavedState myState = new SavedState(superState);
	    // Set the state's value with the class member that holds current
	    // setting value
	    myState.value = mNewValue;
	    return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
	    // Check whether we saved the state in onSaveInstanceState
	    if (state == null || !state.getClass().equals(SavedState.class)) {
	        // Didn't save the state, so call superclass
	        super.onRestoreInstanceState(state);
	        return;
	    }

	    // Cast state to custom BaseSavedState and pass to superclass
	    SavedState myState = (SavedState) state;
	    super.onRestoreInstanceState(myState.getSuperState());

	    // Set this Preference's widget to reflect the restored state
	    mNumberPicker.setValue(myState.value);
	}

	private static class SavedState extends BaseSavedState {
	    // Member that holds the setting's value
	    // Change this data type to match the type saved by your Preference
	    int value;

	    public SavedState(Parcelable superState) {
	        super(superState);
	    }

	    public SavedState(Parcel source) {
	        super(source);
	        // Get the current preference's value
	        value = source.readInt();  // Change this to read the appropriate data type
	    }

	    @Override
	    public void writeToParcel(Parcel dest, int flags) {
	        super.writeToParcel(dest, flags);
	        // Write the preference's value
	        dest.writeInt(value);  // Change this to write the appropriate data type
	    }

	    // Standard creator object using an instance of this class
	    public static final Parcelable.Creator<SavedState> CREATOR =
	            new Parcelable.Creator<SavedState>() {

	        public SavedState createFromParcel(Parcel in) {
	            return new SavedState(in);
	        }

	        public SavedState[] newArray(int size) {
	            return new SavedState[size];
	        }
	    };
	}

	@Override
	public void setDefaultValue(Object defaultValue) {
		this.onSetInitialValue(false, (Integer)defaultValue);
	}
	
	

}
