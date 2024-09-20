package com.example.myapplication

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), NewsItemClicked {

    private lateinit var mAdapter: NewsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchData()
        mAdapter = NewsListAdapter(this)
        recyclerView.adapter = mAdapter
    }

    private fun fetchData() {
        val url = "https://newsdata.io/api/1/latest?apikey=pub_53429832773f60769e70af3ee840b9a2c276d&q=breaking"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener { response ->
                val newsJsonArray = response.getJSONArray("results")
                val newsArray = ArrayList<News>()
                for (i in 0 until newsJsonArray.length()) {
                    val newsJsonObject = newsJsonArray.getJSONObject(i)

                    // Handle creator array safely
                    val creatorsArray = newsJsonObject.optJSONArray("creator")
                    val creator = if (creatorsArray != null && creatorsArray.length() > 0) {
                        creatorsArray.getString(0)
                    } else {
                        "Unknown Author"
                    }

                    val news = News(
                        newsJsonObject.getString("title"),
                        creator,  // Handle creator correctly
                        newsJsonObject.getString("link"),
                        newsJsonObject.optString("image_url", null) // Handle missing image
                    )
                    newsArray.add(news)
                }

                Log.d("MainActivity", "Fetched ${newsArray.size} articles")
                Toast.makeText(this, "Fetched ${newsArray.size} articles", Toast.LENGTH_SHORT).show()

                mAdapter.updateNews(newsArray)
            },
            Response.ErrorListener {
                Log.e("MainActivity", "Error fetching data")
                Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        )
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }



    override fun onItemClicked(item: News) {
        val builder =  CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(item.url))
    }
}
