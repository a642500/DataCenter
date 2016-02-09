package co.yishun.library.datacenter;

import java.util.List;

/**
 * Created by carlos on 2/9/16.
 */
public class PageIntegerLoadIndex<T extends Updatable> implements LoadIndexProvider.LoadIndex<T> {
    private int page;

    public PageIntegerLoadIndex(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    @Override
    public void increment(List<T> result) {
        page++;
    }

    @Override
    public void reset() {
        page = 0;
    }

    @Override
    public boolean equals(LoadIndexProvider.LoadIndex<T> another) {
        return another instanceof PageIntegerLoadIndex && this.page == ((PageIntegerLoadIndex) another).page;
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
