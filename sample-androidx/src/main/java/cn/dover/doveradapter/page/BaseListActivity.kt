package cn.dover.doveradapter.page

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 *
 * @author Baron
 */
class BaseListActivity : AppCompatActivity() {
    private var type = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setHomeButtonEnabled(true)
        type = intent.getIntExtra("type", 0)
        val fragment = when (type) {
            1 -> MultiTypeFragment()
            2 -> HeaderFooterFragment()
            3 -> AutoPreloadFragment()
            else -> SingleTypeFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .commit()
    }
}