package co.yishun.library.datacenter;

/**
 * Created by carlos on 2/4/16.
 */
public interface Updatable extends Comparable {
    /**
     * Compares this updatable to the updatable to determine which is the newer.
     *
     * @param another the object to compare to this instance. The another is sure to be {@link
     *                #equals(Object)} to this updatable.
     * @return a negative integer if this instance is older than {@code another}; a positive integer
     * if this instance is newer than {@code another}; 0 if this instance has the same freshness as
     * {@code another}.
     */
    boolean updateThan(Updatable another);


    /**
     * Compares this instance with the specified updatable and indicates if they are the same
     * position in {@link DataCenter}. When two updatable is equal, one of them which is newer will
     * be kept, the older one will be discard.
     */
    boolean equals(Object o);

}
