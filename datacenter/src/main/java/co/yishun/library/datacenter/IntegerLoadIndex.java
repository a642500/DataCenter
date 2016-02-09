package co.yishun.library.datacenter;

import java.util.List;

/**
 * Created by carlos on 2/9/16.
 */
public class IntegerLoadIndex<T extends Updatable> implements LoadIndexProvider.LoadIndex<T> {
    private int value;

    public IntegerLoadIndex(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void increment(List<T> result) {
        value += result.size();
    }

    @Override
    public void reset() {
        value = 0;
    }

    @Override
    public boolean equals(LoadIndexProvider.LoadIndex<T> another) {
        return another instanceof IntegerLoadIndex && this.value == ((IntegerLoadIndex) another).value;
    }

    @Override
    public LoadIndexProvider.LoadIndex<T> getCopy() {
        try {
            //noinspection unchecked
            return (LoadIndexProvider.LoadIndex<T>) this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}