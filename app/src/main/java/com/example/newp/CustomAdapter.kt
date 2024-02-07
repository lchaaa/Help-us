package com.example.newp
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

//리스트뷰 에 표시 어댑터
class CustomAdapter(private val context: Context) : BaseAdapter() {
    data class CustomItem(val title: String?, val content: String?,val adress: String?,val price: String?, val timestamp: String?
    ,val userId: String?, val imageUrl: String?)
    private val items = mutableListOf<CustomItem>()
    fun addItem(title: String?, content: String? ,adress: String? ,price: String? ,timestamp: String?,userId: String?,imageUrl: String?) {
        val item = CustomItem(title, content,adress,price,timestamp, userId,imageUrl)
        items.add(item)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return items.size
    }
    override fun getItem(position: Int): Any {
        return items[position]
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //리스트뷰 에 표시
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View
        val holder: ViewHolder
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemView = inflater.inflate(R.layout.list_item, null)
            holder = ViewHolder()
            holder.titleTextView = itemView.findViewById(R.id.titleTextView)
            holder.adressTextView = itemView.findViewById(R.id.adressTextView)
            holder.priceTextView = itemView.findViewById(R.id.priceTextView)
            holder.timestampTextView = itemView.findViewById(R.id.timestampTextView)
            holder.imageView = itemView.findViewById(R.id.imageView)
            itemView.tag = holder
        } else {
            itemView = convertView
            holder = itemView.tag as ViewHolder
        }

        val item = items[position]
        holder.titleTextView.text =item.title
        holder.adressTextView.text = "주소: ${item.adress}"
        holder.priceTextView.text = "가격: ${item.price}원"
        holder.timestampTextView.text = "날짜: ${item.timestamp}"

        // 이미지를 Picasso를 사용하여 이미지뷰에 설정합니다.
        if (item.imageUrl != null) {
            Picasso.get().load(item.imageUrl).into(holder.imageView)
        }
        return itemView
    }

    private class ViewHolder {
        lateinit var titleTextView: TextView
        lateinit var adressTextView: TextView
        lateinit var priceTextView: TextView
        lateinit var timestampTextView: TextView
        lateinit var imageView: ImageView
    }
}