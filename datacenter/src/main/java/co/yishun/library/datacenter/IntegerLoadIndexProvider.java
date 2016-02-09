package co.yishun.library.datacenter;

/**
 * Created by carlos on 2/9/16.
 */
public class IntegerLoadIndexProvider<T extends Updatable> implements LoadIndexProvider<IntegerLoadIndex<T>, T> {
    private final IntegerLoadIndex<T> mResetInstance = new IntegerLoadIndex<>(-1);

    @Override
    public IntegerLoadIndex<T> initInstance() {
        return new IntegerLoadIndex<>(0);
    }

    @Override
    public IntegerLoadIndex<T> resetIndex() {
        return mResetInstance;
    }
}


