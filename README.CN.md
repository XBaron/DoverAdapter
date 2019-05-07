# DoverAdapter  [ ![Download](https://api.bintray.com/packages/xbaron/maven/dover-adapter/images/download.svg?version=1.0.0)](https://bintray.com/xbaron/maven/dover-adapter/1.0.0/link)

<img src="images/dover.png" width="100%" height="300" align=center/>

一个轻量、强大、高扩展性、易用的RecyclerView Adapter，采用kotlin语言特性封装

中文文档 | [English_README](README.md)
## 前言

RecyclerView是我们在项目开发中使用最多的列表控件，一个封装好的Adapter可以帮助我们少写大量的样板代码，配合Kotlin的语言特性，我们可以做到一个Adapter简单易用而又不失灵活性。

## 功能

* 多布局支持 - 多数据类型，多布局类型轻松搞定
* AsyncListDiffer支持 - 局部刷新比你想象的简单
* Header、Footer支持 - 头部尾部一键添加
* 自动预加载支持 - 数据不再等待
* ktx支持 - 告别findViewById

## 下载和安装

```Kotlin
// 本库基于Kotlin:1.3.21
// 在你的build.gradle中添加
dependencies {
    implementation "com.github.xbaron:dover-adapter:1.0.0"
    
    // Androidx版本
    implementation "com.github.xbaron:dover-adapter-x:1.0.0"
}

// ViewHolder中使用ktx需要配置
androidExtensions {
    experimental = true
}
```

## 使用

* #### 简单使用

一个简单的Adapter绑定，只需要指定itemViewId以及对应的数据绑定方法即可

```
mAdapter = DoverAdapter<String>()
            .itemView { _, _ -> R.layout.item_single_type }
            .map(String::class) { holder, _, data, _ ->
                with(holder.itemView) {
                    tvText.text = data
                }
            }
            .attachTo(rvList)

// 数据操作，所有数据操作由AsyncListDiffer代理
// 给Adapter设置数据
mAdapter.submitList(listOf("A", "B", "C")) 
// 获取Adapter数据
mAdapter.currentList()
// 其他增删改查方法
mAdapter.addList(data: List<T>, index: Int?)
mAdapter.remove(data: T)
mAdapter.update(data: T)
```

* #### 多布局情况

```Kotlin
mAdapter = DoverAdapter<Any>()
            .itemView { position, data ->
                when (data) {
                    is MultiItem1 -> R.layout.item_multi_type1
                    is MultiItem2 -> R.layout.item_multi_type2
                    is MultiItem3 -> R.layout.item_multi_type3
                }
            }
            .map(MultiItem1::class) { holder, _, data, _ ->
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

多布局情况也很简单，如上代码所示：

1. 你需要为itemView指定多布局itemViewId，这里根据数据类型指定不同的itemViewId
2. 为每个类型的数据添加map绑定数据的方法

* #### 事件绑定

```Kotlin
mAdapter = DoverAdapter<String>()
            .itemView { _, _ -> // 你的itemViewId }
            .map(String::class) { _, _, _, _ -> // 你的数据绑定方法 }
            .onItemClick { _, _, data ->
                Toast.makeText(activity, data, Toast.LENGTH_SHORT).show()
            }
            .onItemLongClick { holder, position, data ->
                Toast.makeText(activity, data, Toast.LENGTH_SHORT).show()
                false
            }
            .attachTo(rvList)
```

简单的点击和长按点击事件，只需要重写onItemClick或onItemLongClick即可

* #### 自定义Item事件

```
 mAdapter = DoverAdapter<Any>()
            .itemView { _, _ -> // 你的itemViewId }
            .map(String::class) { _, _, _, _ -> // 你的数据绑定方法 }
            .onCustomListener { itemView ->
                // 此方法在ViewHolder构造函数中执行
                // 因此这里不能使用mAdapter.getItem(layoutPosition)
                // 此时item还未绑定，layoutPosition = -1
                itemView.checkbox?.setOnCheckedChangeListener { buttonView, isChecked -> 
                    // 你的checkbox OnCheckedChangeListener
                    // 这里可以使用mAdapter.getItem(layoutPosition)获取position对应数据
                }
            }
            .attachTo(rvList)
```

如果你需要为itemView中某一个子View绑定事件，可以重写onCustomListener，然后根据itemView拿到子View，并给它绑定事件。

注：onCustmListener 是在ViewHolder构造函数中执行的，如果在这里需要获取itemView所在position，需要使用ViewHolder.getLayoutPosition\(\)，并且position只能在子View的事件回调中获取

* #### Header&Footer

```Kotlin
 mAdapter = DoverAdapter<String>()
            .itemView { _, _ -> // 你的itemViewId }
            .map(String::class) { _, _, _, _ -> // 你的数据绑定方法 }
            .addHeader(R.layout.layout_header)
            .addHeaders(listOf(R.layout.layout_header))
            .addFooter(R.layout.layout_footer)
            .addFooters(listOf(R.layout.layout_footer))
            .attachTo(rvList)
```

添加自定义头部尾部只需要调用addHeader\(\)，addHeaders\(\)或者addFooter\(\), addFooters\(\)，支持动态添加删除

* #### 预加载

预加载即当RecyclerView滑动到底部时，自动触发加载数据，在DoverAdapter中你可以使用onPreload\(preloadDistance: Int\)来自定义预加载逻辑，这里的preloadDistance为当前触发距离，即getItemCount\(\) - 当前位置 &lt;= preloadDistance时，触发onPreload逻辑

```
mAdapter = DoverAdapter<Any>()
            .itemView { _, _ -> // 你的itemViewId }
            .map(String::class) { _, _, _, _ -> // 你的数据绑定方法 }
            .onPreload(3) { // 距离底部距离为<=3时，触发预加载
                // 你的加载数据方法
            }
            .attachTo(rvList)
```

* #### 自定义Diff规则
```kotlin
mAdapter = DoverAdapter<Any>()
            .itemView { _, _ -> // 你的itemViewId }
            .map(String::class) { _, _, _, _ -> // 你的数据绑定方法 }
            .diffItems { oldItem, newItem -> 
                // 对应DiffUtl.Callback的areItemsTheSame 
            }
            .diffContents { oldItem, newItem -> 
                // 对应DiffUtl.Callback的areContentsTheSame 
            }
            .diffPayload { oldItem, newItem -> 
                // 对应DiffUtl.Callback的getChangePayload 
            }
            .attachTo(rvList)
```
自定义Diff规则，你可以重写diffItems，diffContents，diffPayload这三个方法，对应DiffUtil.Callback的三个回调

### Proguard

本库无需配置proguard规则

### License

[Apache License 2.0](LICENSE)

### Thanks

[MultiType](https://github.com/drakeet/MultiType)

欢迎issue、pr，或者请我喝茶  
  
  
| <img src="images/wechat.png" width="300" height="300" />| <img src="images/alipay.jpg" width="300" height="300" />| 
|:------:|:------:|
| 微信| 支付宝|