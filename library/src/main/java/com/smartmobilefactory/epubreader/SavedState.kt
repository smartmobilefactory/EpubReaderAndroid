package com.smartmobilefactory.epubreader

import android.os.Parcel
import android.os.Parcelable
import android.support.v4.os.ParcelableCompat
import android.support.v4.os.ParcelableCompatCreatorCallbacks
import android.support.v4.view.AbsSavedState
import com.smartmobilefactory.epubreader.model.EpubLocation

internal class SavedState : AbsSavedState {

    var epubUri: String? = null
    var location: EpubLocation? = null
    var loader: ClassLoader? = null

    internal constructor(superState: Parcelable) : super(superState) {}

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeString(epubUri)
        out.writeParcelable(location, flags)
    }

    override fun toString(): String {
        return ("EpubView.SavedState{"
                + Integer.toHexString(System.identityHashCode(this))
                + " location=" + location + "}")
    }

    internal constructor(inParcel: Parcel, loader: ClassLoader?) : super(inParcel, loader) {
        this.loader = loader
        if (loader == null) {
            this.loader = javaClass.classLoader
        }
        epubUri = inParcel.readString()
        location = inParcel.readParcelable(loader)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<SavedState> = ParcelableCompat.newCreator(
                object : ParcelableCompatCreatorCallbacks<SavedState> {
                    override fun createFromParcel(`in`: Parcel, loader: ClassLoader): SavedState {
                        return SavedState(`in`, loader)
                    }

                    override fun newArray(size: Int): Array<SavedState?> {
                        return arrayOfNulls(size)
                    }
                })
    }
}