package cn.dover.doveradapter.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import cn.dover.doveradapter.R
import com.github.xbaron.dover.adapter.DoverAdapter
import kotlinx.android.synthetic.main.item_single_type.view.*
import kotlinx.android.synthetic.main.layout_recyclerview.*

/**
 *
 * @author Baron
 */
class AutoPreloadFragment : Fragment() {
    private lateinit var mAdapter: DoverAdapter<String>
    private var index = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_recyclerview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        getData()
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
            .onPreload(3) {
                getData()
            }
            .attachTo(rvList)
    }

    private fun getData() {
        val list = mAdapter.currentList()
        for (i in index until index + 10) {
            list.add("item $i")
        }
        index = list.size
        mAdapter.submitList(list)
    }
}