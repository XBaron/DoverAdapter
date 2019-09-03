package com.github.xbaron.dover.adapter

import android.support.v7.util.ListUpdateCallback

/**
 * Callback of AsyncDiffer, used to update adapter items
 * @author Baron
 */
class DoverListUpdateCallback<T : Any>(private val mAdapter: DoverAdapter<T>) : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {
        mAdapter.notifyItemRangeInserted(mAdapter.mHeaderViews.size + position, count)
    }

    override fun onRemoved(position: Int, count: Int) {
        mAdapter.notifyItemRangeRemoved(mAdapter.mHeaderViews.size + position, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        mAdapter.notifyItemMoved(mAdapter.mHeaderViews.size + fromPosition, mAdapter.mHeaderViews.size + toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        mAdapter.notifyItemRangeChanged(mAdapter.mHeaderViews.size + position, count, payload)
    }
}