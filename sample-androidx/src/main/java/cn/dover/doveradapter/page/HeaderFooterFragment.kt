package cn.dover.doveradapter.page

import android.os.Bundle
import android.view.*
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
class HeaderFooterFragment : Fragment() {
    private lateinit var mAdapter: DoverAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.layout_recyclerview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.header_footer, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId
        when (itemId) {
            R.id.menu_add_header -> mAdapter.addHeader(R.layout.layout_header)
            R.id.menu_add_footer -> mAdapter.addFooter(R.layout.layout_footer)
            R.id.menu_remove_header -> mAdapter.removeHeader(0)
            R.id.menu_remove_footer -> mAdapter.removeFooter(0)
            R.id.menu_remove_all_headers -> mAdapter.removeAllHeaders()
            R.id.menu_remove_all_footers -> mAdapter.removeAllFooters()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        mAdapter = DoverAdapter<String>()
            .itemView { _, _ -> R.layout.item_single_type }
            .addHeader(R.layout.layout_header)
            .addHeaders(listOf(R.layout.layout_header))
            .addFooter(R.layout.layout_footer)
            .addFooters(listOf(R.layout.layout_footer))
            .map(String::class) { holder, _, data, _ ->
                with(holder.itemView) {
                    tvText.text = data
                }
            }
            .onItemClick { _, _, data ->
                Toast.makeText(activity, data, Toast.LENGTH_SHORT).show()
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