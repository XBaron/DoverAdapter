package cn.dover.doveradapter

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.dover.doveradapter.page.BaseListActivity
import com.github.xbaron.dover.adapter.DoverAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_main_list.view.*

class MainActivity : AppCompatActivity() {
    lateinit var mAdapter: DoverAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initData()
    }

    private fun initView() {
        mAdapter = DoverAdapter<String>()
            .itemView { _, _ -> R.layout.item_main_list }
            .map(String::class) { holder, _, data, _ ->
                with(holder.itemView) {
                    tvName.text = data
                }
            }
            .onItemClick { _, position, _ ->
                val intent = Intent(this, BaseListActivity::class.java)
                intent.putExtra("type", position)
                startActivity(intent)
            }
            .attachTo(rvList)
    }

    private fun initData() {
        mAdapter.submitList(listOf("Single Type", "Multi Type", "Header & Footer", "Auto Preload"))
    }
}
