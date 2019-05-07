package cn.dover.doveradapter.page

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cn.dover.doveradapter.R
import cn.dover.doveradapter.entity.MultiItem1
import cn.dover.doveradapter.entity.MultiItem2
import cn.dover.doveradapter.entity.MultiItem3
import com.github.xbaron.dover.adapter.DoverAdapter
import kotlinx.android.synthetic.main.item_multi_type1.view.*
import kotlinx.android.synthetic.main.item_multi_type2.view.*
import kotlinx.android.synthetic.main.item_multi_type3.view.*
import kotlinx.android.synthetic.main.item_single_type.view.*
import kotlinx.android.synthetic.main.layout_recyclerview.*

/**
 *
 * @author Baron
 */
class MultiTypeFragment : Fragment() {
    private lateinit var mAdapter: DoverAdapter<Any>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_recyclerview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        mAdapter = DoverAdapter<Any>()
            .itemView { _, data ->
                when (data) {
                    is MultiItem1 -> R.layout.item_multi_type1
                    is MultiItem2 -> R.layout.item_multi_type2
                    is MultiItem3 -> R.layout.item_multi_type3
                    else -> R.layout.item_single_type
                }
            }
            .map(String::class) { holder, _, data, _ ->
                holder.itemView.tvText?.text = data as String
            }
            .map(MultiItem1::class) { holder, _, data, _ ->
                val item = data as MultiItem1
                with(holder.itemView) {
                    tvTitle1?.text = "Title ${item.title}"
                    tvContent1?.text = "Content ${item.content}"
                    ivImage1?.setImageResource(item.image)
                }
            }
            .map(MultiItem2::class) { holder, _, data, _ ->
                val item = data as MultiItem2
                with(holder.itemView) {
                    tvTitle2?.text = item.title
                    ivImage2?.setImageResource(item.image)
                }
            }
            .map(MultiItem3::class) { holder, _, data, _ ->
                val item = data as MultiItem3
                with(holder.itemView) {
                    tvTitle31?.text = item.title1
                    tvTitle32?.text = item.title2
                    ivImage31?.setImageResource(item.image1)
                    ivImage32?.setImageResource(item.image2)
                }
            }
            .onItemClick { _, position, data ->
                if (data !is MultiItem3) {
                    showToast("click position $position")
                }
            }
            .onCustomListener { itemView ->
                itemView.tvTitle1?.setOnClickListener { showToast("Title - click position $layoutPosition") }
                itemView.tvContent1?.setOnClickListener { showToast("Content - click position $layoutPosition") }

                val listener1 = View.OnLongClickListener {
                    showToast("Long click position $layoutPosition 111")
                    false
                }
                val listener2 = View.OnLongClickListener {
                    showToast("Long click position $layoutPosition 222")
                    false
                }

                itemView.tvTitle31?.setOnLongClickListener(listener1)
                itemView.ivImage31?.setOnLongClickListener(listener1)
                itemView.tvTitle32?.setOnLongClickListener(listener2)
                itemView.ivImage32?.setOnLongClickListener(listener2)
            }
            .attachTo(rvList)
    }

    private fun initData() {
        val list = ArrayList<Any>()
        for (i in 0..20) {
            var text = "Click me $i"
            when (i % 4) {
                0 -> list.add(text)
                1 -> list.add(MultiItem1(text, text, R.mipmap.ic_launcher))
                2 -> list.add(MultiItem2(text, R.mipmap.ic_launcher))
                3 -> {
                    text = "Long click me $i"
                    list.add(MultiItem3(text, text, R.mipmap.ic_launcher, R.mipmap.ic_launcher))
                }
            }
        }
        mAdapter.submitList(list)
    }

    private fun showToast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }
}