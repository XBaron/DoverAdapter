package com.github.xbaron.dover.adapter

import android.support.annotation.LayoutRes
import android.support.v7.recyclerview.extensions.AsyncDifferConfig
import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import java.util.*
import kotlin.reflect.KClass

/**
 *  A convenient adapter to simplify RecyclerView item binding
 *
 *  <p>
 *  features:
 *   1.support multi item type, @see [itemView]
 *   2.support auto preload (custom preload distance [mPreloadDistance]), @see [autoPreload]
 *   3.support customize diff callback to simplify usage of [AsyncListDiffer], @see [diffItems], [diffContents], [diffPayload]
 *   4.support customize header and footer view
 *   4.no findViewByIds nor ButterKnife bindings (use ktx Android Extensions-style view access),
 *      (you should add androidExtensions{ experimental = true } in your build.gradle for now)
 *  </p>
 *
 *  e.g:
 *
 */
open class DoverAdapter<T : Any> : RecyclerView.Adapter<DoverAdapter<T>.DoverViewHolder>() {
    /**
     * list of header views
     */
    open var mHeaderViews = mutableListOf<Int>()
    /**
     * list of footer views
     */
    open var mFooterViews = mutableListOf<Int>()

    /**
     * preload distance from current position to itemCount, when position reach to ([getItemCount] - [mPreloadDistance]),
     * auto trigger [mPreload] functions
     */
    private var mPreloadDistance = 0

    /**
     * used to record previous itemCount
     * when item position reach to ([getItemCount] - [mPreloadDistance]), [mPreload] function invokes,
     * and position continue to increase, [mPreload] should be not trigger again until position reach to new ([getItemCount] - [mPreloadDistance])
     * @see [autoPreload]
     */
    private var mPreviousItemCount = 0

    /** an instance of [LayoutInflater], used to avoid duplicate creation */
    private var mInflater: LayoutInflater? = null

    /**
     * default diff callback
     * just use [Any.equals] to calculate [DiffUtil.ItemCallback.areItemsTheSame] and [DiffUtil.ItemCallback.areContentsTheSame] results
     * so if you just want to use this default diff callback, you should overrides the [equals] function of your data class
     *
     * you can use the function [diffItems] to set result of [DiffUtil.ItemCallback.areItemsTheSame] and
     * [diffContents] to set result of [DiffUtil.ItemCallback.areContentsTheSame] to customize the diff callback
     */
    private var mItemsDiff: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem }
    private var mContentsDiff: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem }
    private var mChangePayload: (oldItem: T, newItem: T) -> Any? = { _, _ -> null }
    private var mDiffCallback: DiffUtil.ItemCallback<T> = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = mItemsDiff.invoke(oldItem, newItem)
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = mContentsDiff.invoke(oldItem, newItem)
        override fun getChangePayload(oldItem: T, newItem: T): Any? = mChangePayload.invoke(oldItem, newItem)
    }

    /** an instance of [AsyncListDiffer], used to calculate diff result in async way */
    private var mDiffer: AsyncListDiffer<T> =
        AsyncListDiffer(DoverListUpdateCallback(this), AsyncDifferConfig.Builder(mDiffCallback).build())

    private var mItemView: DoverAdapter<T>.(position: Int, data: T) -> Int = { _, _ -> 0 }

    /**
     * binding functions,
     * key: your data class,
     * value: your binding function of this data class
     */
    private val mBindings =
        HashMap<KClass<*>, DoverAdapter<T>.(holder: DoverViewHolder, position: Int, data: T, payloads: MutableList<Any>?) -> Unit>()

    /**
     * item click function
     * @see [onItemLongClick], [DoverViewHolder]
     */
    private var mItemClick: (holder: DoverViewHolder, position: Int, data: T) -> Unit = { _, _, _ -> }

    /**
     * item long click function,
     * @see [onItemClick], [DoverViewHolder]
     */
    private var mItemLongClick: (holder: DoverViewHolder, position: Int, data: T) -> Boolean = { _, _, _ -> true }

    /**
     * custom item event function,
     * @see [onCustomListener], [DoverViewHolder]
     */
    private var mCustomListener: DoverViewHolder.(itemView: View) -> Unit = { _ -> }
    /**
     * onPreload function
     * @see [autoPreload]
     */
    private var mPreload: DoverAdapter<T>.() -> Unit = {}

    /**
     * get data of this position
     * @return data instance of this position
     */
    open fun getItem(position: Int): T = mDiffer.currentList[position]

    override fun getItemCount(): Int = mDiffer.currentList.size + mHeaderViews.size + mFooterViews.size

    /**
     * items is:
     * header | header | ..| data | .. | footer| ..|
     * so the start position of data is [mHeaderViews] size,
     * and the end position of data is [mHeaderViews].size + [mDiffer].currentList().size - 1
     */
    private var dataStartPosition = { mHeaderViews.size }
    private var footerStartPosition = { mDiffer.currentList.size + mHeaderViews.size }

    /**
     * get itemViewType from your DoverAdapterItem
     */
    override fun getItemViewType(position: Int): Int {
        return when {
            position < dataStartPosition() -> mHeaderViews[position]
            position >= footerStartPosition() -> mFooterViews[position - footerStartPosition()]
            else -> mItemView.invoke(this, position - dataStartPosition(), getItem(position - dataStartPosition()))
        }
    }

    /**
     * use [viewType] as a layoutId to inflate a view
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoverViewHolder {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.context)
        }
        return DoverViewHolder(mInflater!!.inflate(viewType, parent, false))
    }

    override fun onBindViewHolder(holder: DoverViewHolder, position: Int) {}

    override fun onBindViewHolder(holder: DoverViewHolder, position: Int, payloads: MutableList<Any>) {
        if (position in dataStartPosition() until footerStartPosition()) {
            val itemPosition = position - dataStartPosition()
            autoPreload(itemPosition)
            val item = getItem(itemPosition)
            // get binding function of item data class
            val itemBinding = mBindings[item::class]
            itemBinding?.invoke(this, holder, itemPosition, item, payloads)
        }
    }

    open fun itemView(itemView: DoverAdapter<T>.(position: Int, data: T) -> Int) = apply { this.mItemView = itemView }

    /**
     * map your data class and it's binding functions
     * <p>
     * e.g:
     * XAdapter<User>.map(User::class){ holder, position, data ->
     *     with(holder.itemView) {
     *          tvUsername.text = data.name
     *     }
     * }
     * </p>
     * @param clazz data class
     * @param binding binding function for the data class
     */
    open fun map(
        clazz: KClass<*>,
        binding: DoverAdapter<T>.(holder: DoverViewHolder, position: Int, data: T, payloads: MutableList<Any>?) -> Unit
    ): DoverAdapter<T> = apply {
        this.mBindings[clazz] = binding
    }

    /**
     * set click listener to itemView
     * Note that this [itemClick] function is invoked in [onCreateViewHolder]
     * <p>
     * e.g:  XAdapter<User>.map(User::class){ holder, position, data: User ->
     *          with(holder.itemView) {
     *              tvUsername.text = data.name
     *          }
     *      }.onItemClick { holder, position, data ->
     *
     *      }
     * </p>
     * @param itemClick click function
     */
    open fun onItemClick(itemClick: (holder: DoverViewHolder, position: Int, data: T) -> Unit): DoverAdapter<T> =
        apply {
            this.mItemClick = itemClick
        }

    /**
     * set long click listener to itemView
     * @param itemLongClick click function
     */
    open fun onItemLongClick(itemLongClick: (holder: DoverViewHolder, position: Int, data: T) -> Boolean): DoverAdapter<T> =
        apply {
            this.mItemLongClick = itemLongClick
        }

    /**
     * set custom listener to itemView
     * you can easily access to the child views of itemView, and set click listener or other listener to them
     * @param customListener
     */
    open fun onCustomListener(customListener: DoverViewHolder.(itemView: View) -> Unit) = apply {
        this.mCustomListener = customListener
    }

    /**
     * set preload callback
     * @param preloadDistance see [mPreloadDistance]
     * @param preload see [autoPreload]
     */
    open fun onPreload(preloadDistance: Int, preload: DoverAdapter<T>.() -> Unit): DoverAdapter<T> = apply {
        this.mPreloadDistance = preloadDistance
        this.mPreload = preload
    }

    /**
     * customize [DiffUtil.ItemCallback.areItemsTheSame]
     */
    open fun diffItems(itemsDiff: (oldItem: T, newItem: T) -> Boolean) = apply {
        this.mItemsDiff = itemsDiff
    }

    /**
     * customize [DiffUtil.ItemCallback.areContentsTheSame]
     */
    open fun diffContents(contentsDiff: (oldItem: T, newItem: T) -> Boolean) = apply {
        this.mContentsDiff = contentsDiff
    }

    /**
     * customize [DiffUtil.ItemCallback.areContentsTheSame]
     */
    open fun diffPayload(payloadDiff: (oldItem: T, newItem: T) -> Any?) = apply {
        this.mChangePayload = payloadDiff
    }

    /**
     * when item position reach to ([getItemCount] - [mPreloadDistance]), [mPreload] function invokes,
     * and position continue to increase, [mPreload] should be not trigger again until position reach to new ([getItemCount] - [mPreloadDistance])
     */
    private fun autoPreload(position: Int) {
        if (mPreloadDistance <= 0 || position < itemCount - mPreloadDistance) {
            return
        }
        if (itemCount > mPreviousItemCount) {
            mPreviousItemCount = itemCount
            mPreload.invoke(this)
        }
    }

    open fun attachTo(recyclerView: RecyclerView) = apply {
        recyclerView.adapter = this
    }

    /**
     * header and footer operation methods
     */
    open fun addHeader(@LayoutRes headerView: Int, index: Int? = mHeaderViews.size) = apply {
        mHeaderViews.add(index!!, headerView)
        notifyItemInserted(mHeaderViews.size)
    }

    open fun addFooter(@LayoutRes footerView: Int, index: Int? = mFooterViews.size) = apply {
        mFooterViews.add(index!!, footerView)
        notifyItemInserted(mHeaderViews.size + mDiffer.currentList.size)
    }

    open fun addHeaders(headerViews: List<Int>, index: Int? = mHeaderViews.size) = apply {
        mHeaderViews.addAll(index!!, headerViews)
        notifyItemRangeInserted(mHeaderViews.size, headerViews.size)
    }

    open fun addFooters(footerViews: List<Int>, index: Int? = mFooterViews.size) = apply {
        mFooterViews.addAll(index!!, footerViews)
        notifyItemRangeInserted(footerStartPosition(), footerViews.size)
    }

    open fun removeHeader(headerPosition: Int) = apply {
        if (mHeaderViews.size > headerPosition) {
            mHeaderViews.removeAt(headerPosition)
            notifyItemRemoved(headerPosition)
        }
    }

    open fun removeFooter(footerPosition: Int) = apply {
        if (mFooterViews.size > footerPosition) {
            mFooterViews.removeAt(footerPosition)
            notifyItemRemoved(footerStartPosition() + footerPosition)
        }
    }

    open fun removeAllHeaders() = apply {
        if (!mHeaderViews.isEmpty()) {
            val headerSize = mHeaderViews.size
            mHeaderViews.clear()
            notifyItemRangeRemoved(0, headerSize)
        }
    }

    open fun removeAllFooters() = apply {
        if (!mFooterViews.isEmpty()) {
            val footerSize = mFooterViews.size
            mFooterViews.clear()
            notifyItemRangeRemoved(footerStartPosition(), footerSize)
        }
    }

    /**
     * item data operation methods
     */
    open fun add(data: T, index: Int? = itemCount) = submitList(currentList().apply { add(index!!, data) })
    open fun addList(data: List<T>, index: Int? = itemCount) = submitList(currentList().apply { addAll(index!!, data) })
    open fun remove(data: T) = submitList(currentList().apply { remove(data) })
    open fun remove(position: Int) = submitList(currentList().apply { removeAt(position) })
    open fun update(data: T) = submitList(currentList().apply { set(indexOf(data), data) })
    open fun update(position: Int) = submitList(currentList().apply { set(position, get(position)) })
    open fun currentList(): MutableList<T> = ArrayList(mDiffer.currentList)
    open fun submitList(data: List<T>) = mDiffer.submitList(data)

    /**
     * base ViewHolder to simplify Android Extensions-style view access and event handling
     */
    inner class DoverViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {
        init {
            itemView.apply {
                setOnClickListener {
                    if (dataPosition() >= 0 && dataPosition() < currentList().size) {
                        mItemClick.invoke(this@DoverViewHolder, dataPosition(), getItem(dataPosition()))
                    }
                }
                setOnLongClickListener {
                    if (dataPosition() >= 0 && dataPosition() < currentList().size) {
                        return@setOnLongClickListener mItemLongClick.invoke(
                            this@DoverViewHolder,
                            dataPosition(),
                            getItem(dataPosition())
                        )
                    }
                    false
                }
            }
            // invoke custom itemView event handling, you can access child views of itemView in [mCustomListener]
            mCustomListener.invoke(this, itemView)
        }

        private fun dataPosition() = layoutPosition - dataStartPosition.invoke()
    }
}
