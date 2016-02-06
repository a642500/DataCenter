package co.yishun.library.datacenter;

/**
 * Created by carlos on 2/4/16.
 */
public interface Updatable extends Comparable {
    boolean updateThan(Updatable updatable);
}
