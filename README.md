# DoverAdapter [![Download](https://api.bintray.com/packages/xbaron/maven/dover-adapter/images/download.svg?version=1.0.0)](https://bintray.com/xbaron/maven/dover-adapter/1.0.0/link)

<img src="images/dover.png" width="100%" height="300" align=center/>

A lighweight,powerful,extensible,easy to use recyclerview adapter

English_README | [中文文档](README.CN.md)
## Introduction

RecyclerView is a list widget we use most in project development ，a well wrapped recyclerview adapter can help us write a lot less boilerplate code。
Kotlin features like DSL, null safety, lambdas function can make a adapter more concise and extensible

## Features

* Multiple items support
* AsyncListDiffer support
* Header、Footer support
* Auto preload support
* ktx support - no more findViewByIds

## Install

```Kotlin
// base on Kotlin:1.3.21
// in your build.gradle
dependencies {
    implementation "com.github.xbaron:dover-adapter:1.0.0"

    // Androidx version
    implementation "com.github.xbaron:dover-adapter-x:1.0.0"
}

// if you need to use ktx in ViewHolder, you need add this to your module's build.gradle
// @see https://kotlinlang.org/docs/tutorials/android-plugin.html
androidExtensions {
    experimental = true
}
```

## Usage

* #### Simple usage

To bind a recyclerView adapter in a most simple way, you just need to set a **itemView** function to specify the layout id of item,
and a **map** function to bind data to item

```
mAdapter = DoverAdapter<String>()
            .itemView { position, data -> R.layout.item_single_type }
            .map(String::class) { holder, position, data, payloads ->
                with(holder.itemView) {
                    tvText.text = data
                }
            }
            .attachTo(rvList)

// all data operations are delegate to **AsyncListDiffer**, like PagedListAdapter
// submit a list to adapter
mAdapter.submitList(listOf("A", "B", "C"))
// get the current list of adapter
mAdapter.currentList()
// other data operation methods
mAdapter.addList(data: List<T>, index: Int?)
mAdapter.remove(data: T)
mAdapter.update(data: T)
```

* #### Multiple items

DoverAdapter makes multiple items binding a simple way，you just need：
1. Set a custom **itemView** function to specify the layout id of item, you can use position and data parameters to specify different layout for each item
2. Set a custom **map** functions to specify the data binding

```Kotlin
mAdapter = DoverAdapter<Any>()
            .itemView { position, data ->
                when (data) {
                    is MultiItem1 -> R.layout.item_multi_type1
                    is MultiItem2 -> R.layout.item_multi_type2
                    is MultiItem3 -> R.layout.item_multi_type3
                }
            }
            .map(MultiItem1::class) { holder, position, data, payloads ->
                val item = data as MultiItem1
                with(holder.itemView) {
                    tvTitle1.text = item.title
                    tvContent1.text = item.content
                    ivImage1.setImageResource(item.image)
                }
            }
            .map(MultiItem2::class) { holder, _, data, _ ->
                val item = data as MultiItem2
                with(holder.itemView) {
                    tvTitle2.text = item.title
                    ivImage2.setImageResource(item.image)
                }
            }
            .map(MultiItem3::class) { holder, _, data, _ ->
                val item = data as MultiItem3
                with(holder.itemView) {
                    tvTitle31.text = item.title1
                    tvTitle32.text = item.title2
                    ivImage31.setImageResource(item.image1)
                    ivImage32.setImageResource(item.image2)
                }
            }
            .attachTo(rvList)

val list = ArrayList<Any>()
list.add(MultiItem1())
list.add(MultiItem2())
list.add(MultiItem3())
mAdapter.submitList(list)
```

* #### Item click

Listen for item clicks or long clicks, just set a custom **onItemClick** or **onItemLongClick** to DoverAdapter

```Kotlin
mAdapter = DoverAdapter<String>()
            .itemView { _, _ -> // your itemViewId }
            .map(String::class) { _, _, _, _ -> // your custom map function }
            .onItemClick { _, _, data ->
                Toast.makeText(activity, data, Toast.LENGTH_SHORT).show()
            }
            .onItemLongClick { holder, position, data ->
                Toast.makeText(activity, data, Toast.LENGTH_SHORT).show()
                false
            }
            .attachTo(rvList)
```

* #### Custom child view listener

It is pretty simple to set a custom listener to a child view of itemView,just set a custom **onCustomListener**
to your adapter,then you can get your child view from itemView and set listener to it

Note: **onCustomListener** is invoked in ViewHolder's constructor,it is called in onCreateViewHolder() not in onBindViewHolder()
to avoid duplicate listener binding,so ViewHolder.getLayoutPosition() must call after item was binded to recyclerView
```
 mAdapter = DoverAdapter<Any>()
            .itemView { _, _ -> // your itemViewId }
            .map(String::class) { _, _, _, _ -> // your custom map function }
            .onCustomListener { itemView ->
                // invoke in ViewHolder's constructor, so getLayoutPosition() returns -1 because your item has not been bind this time
                itemView.checkbox?.setOnCheckedChangeListener { buttonView, isChecked ->
                    // your custom checkbox OnCheckedChangeListener
                    // when the listener of a view has been invoked, means the item was binded to recyclerview
                    // now you can access your item data by mAdapter.getItem(layoutPosition)
                }
            }
            .attachTo(rvList)
```


* #### Header&Footer

Header and Footer is also simple to use,call **addHeader(id)** of **addHeaders(listOf(ids))** to add a header or a list of header,
and same operations to footer
Note: no more notifyItemChange(), it is already called in every function of header and footer operations

```Kotlin
 mAdapter = DoverAdapter<String>()
            .itemView { _, _ -> // your itemViewId }
            .map(String::class) { _, _, _, _ -> // your custom map function }
            .addHeader(R.layout.layout_header)
            .addHeaders(listOf(R.layout.layout_header))
            .addFooter(R.layout.layout_footer)
            .addFooters(listOf(R.layout.layout_footer))
            .attachTo(rvList)
```

* #### Auto preload

When recyclerview scroll to it's bottom, we may want it to load the next page of data automatically,in DoverAdapter,
just set a custom **onPreload(preloadDistance)** function,the preloadDistance parameter is the distance from current position to
the bottom of recyclerview to trigger preload,when itemCount - currentPosition <= preloadDistance,onPreload function triggers

Note: when onPreload function triggers,the next trigger will occur when itemCount changes, that is onPreload will be trigger just
once until itemCount changes

```
mAdapter = DoverAdapter<Any>()
            .itemView { _, _ -> // youritemViewId }
            .map(String::class) { _, _, _, _ -> // your custom map function }
            .onPreload(3) { // when itemCount - currentPosition <= 3, onPreload triggers
                // your custom data load function
            }
            .attachTo(rvList)
```

* #### Custom diff callbacks

there are 3 functions correspond to DiffUtil.ItemCallback
**diffItems** -> DiffUtil.ItemCallback.areItemsTheSame
**diffContents** -> DiffUtil.ItemCallback.areContentsTheSame
**diffPayload** -> DiffUtil.ItemCallback.getChangePayload

```kotlin
mAdapter = DoverAdapter<Any>()
            .itemView { _, _ -> // 你的itemViewId }
            .map(String::class) { _, _, _, _ -> // 你的数据绑定方法 }
            .diffItems { oldItem, newItem ->
                // your custom areItemsTheSame callback
            }
            .diffContents { oldItem, newItem ->
                // your custom areContentsTheSame callback
            }
            .diffPayload { oldItem, newItem ->
                // your custom getChangePayload callback
            }
            .attachTo(rvList)
```

### Proguard

No need for proguard rules

### License

[Apache License 2.0](LICENSE)

### Thanks

[MultiType](https://github.com/drakeet/MultiType)