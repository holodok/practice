package com.gogaworm.praktika.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created on 06.03.2017.
 *
 * @author ikarpova
 */
public class Person implements Parcelable {
    public long id;
    public int type;
    public String name;

    public Person(long id, int type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    public Person(int type, String name) {
        this.type = type;
        this.name = name;
    }

    protected Person(Parcel in) {
        id = in.readLong();
        type = in.readInt();
        name = in.readString();
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeInt(type);
        parcel.writeString(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Person) {
            Person toCompare = (Person) obj;
            return this.id == toCompare.id;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (int) id;
    }
}
