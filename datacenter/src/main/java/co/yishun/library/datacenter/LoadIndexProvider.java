package co.yishun.library.datacenter;

import java.util.List;

/**
 * Created by carlos on 2/9/16.
 */
public interface LoadIndexProvider<I extends LoadIndexProvider.LoadIndex<T>, T extends Updatable> {
    /**
     * get initiative instance.
     */
    I initInstance();

    I resetIndex();

    interface LoadIndex<T extends Updatable> {
        void increment(List<T> result);

        void reset();
    }
}
