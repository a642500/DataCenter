# DataCenter

A controller to handle data loading and forwarding data changes to adapter.


## Dependency

```gradle

    repositories {
        jcenter()
    }
    
    dependencies {
        compile 'co.yishun.library:datacenter:0.0.4'
    }


```

## Usage

```java


        SuperRecyclerView superRecyclerView = (SuperRecyclerView) findViewById(R.id.superRecyclerView);
        superRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        final DataCenterAdapter<SampleStringData, SampleViewHolder> adapter = new SampleDataCenterAdapter(this);
        adapter.setLoader(this);
        adapter.setOnEndListener(this);
        adapter.setRefreshable(new SuperRecyclerViewRefreshable(superRecyclerView));
        adapter.setLoadMore(new SuperRecyclerViewLoadMore(superRecyclerView));
        adapter.setLoadIndexProvider(new PageIntegerLoadIndexProvider<SampleStringData>());
        superRecyclerView.setAdapter(adapter);

        adapter.loadNext();

```

You should implement 

```java

    interface DataLoader<I extends LoadIndexProvider.LoadIndex<T>, T extends Updatable> {
        List<T> loadOptional(I index);

        List<T> loadNecessary(I index);
    }
```

to load targeted page. ```List<T> loadOptional(I index);``` often loads from cache which is 
responsive but less freshness. ```List<T> loadOptional(I index);``` loads from network which is 
always updated but cost time.
    
    
The result item ```T``` must implement Updatable interface which can be ordered and comparable its freshness. 
This method determines which ```T``` will be kept, if the DataLoader return multiple items at one position.

```
        boolean updateThan(Updatable another);
```

