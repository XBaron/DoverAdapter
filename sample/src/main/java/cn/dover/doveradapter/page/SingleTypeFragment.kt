package cn.dover.doveradapter.page

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cn.dover.doveradapter.R
import com.github.xbaron.dover.adapter.DoverAdapter
import kotlinx.android.synthetic.main.item_single_type.view.*
import kotlinx.android.synthetic.main.layout_recyclerview.*

/**
 *
 * @author Baron
 */
class SingleTypeFragment : Fragment() {
    private lateinit var mAdapter: DoverAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_recyclerview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        mAdapter = DoverAdapter<String>()
            .itemView { _, _ -> R.layout.item_single_type }
            .map(String::class) { holder, _, data, _ ->
                with(holder.itemView) {
                    tvText.text = data
                }
            }
            .onItemClick { _, _, data ->
                Toast.makeText(activity, data, Toast.LENGTH_SHORT).show()
            }
            .onItemLongClick { holder, position, data ->
                Toast.makeText(activity, data, Toast.LENGTH_SHORT).show()
                false
            }
            .attachTo(rvList)
    }

    private fun initData() {
        val list = ArrayList<String>()
        for (i in 0..10) {
            list.add("item $i")
        }
        mAdapter.submitList(list)
    }
}