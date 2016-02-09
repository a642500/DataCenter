package co.yishun.library.datacenter;

/**
 * Created by carlos on 2/9/16.
 */
public class PageIntegerLoadIndexProvider<T extends Updatable> implements LoadIndexProvider<PageIntegerLoadIndex<T>, T> {
    private final PageIntegerLoadIndex<T> mResetInstance = new PageIntegerLoadIndex<>(-1);

    @Override
    public PageIntegerLoadIndex<T> initInstance() {
        return new PageIntegerLoadIndex<>(0);
    }

    @Override
    public PageIntegerLoadIndex<T> resetIndex() {
        return mResetInstance;
    }
}
