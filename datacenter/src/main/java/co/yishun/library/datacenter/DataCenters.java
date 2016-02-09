package co.yishun.library.datacenter;

/**
 * Created by Carlos on 1/28/16.
 */
public class DataCenters {
    //    public static <T> DataCenter<T> getDataCenter(Collection<T> baseData) {
//        return new DataCenterImpl<>(baseData);
//    }
//
    public static <I extends LoadIndexProvider.LoadIndex<T>, T extends Updatable> DataCenter<I, T> getDataCenter() {
        return new DataCenterImpl<>();
    }
}
