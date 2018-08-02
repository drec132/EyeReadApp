package codeeater.com.eyereadapp.adapter

import android.app.Activity
import android.support.annotation.LayoutRes
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import codeeater.com.eyereadapp.R
import codeeater.com.eyereadapp.model.Response
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class ResponseListAdapter(
        private val context: Activity,
        @param:LayoutRes private val resource: Int,
        private val listResponse: List<Response>
) : ArrayAdapter<Response>(context, resource, listResponse) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater

        // 2nd parameter can be recyclerview
        val view = inflater.inflate(resource, null)

        val tvFace = view.findViewById<TextView>(R.id.tvFace)
        val tvImage = view.findViewById<TextView>(R.id.tvImage)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)

        val img = view.findViewById<ImageView>(R.id.imgView)

        tvFace.text = "Face Detection: ${listResponse[position].face}"
        tvImage.text = "Image Response: ${listResponse[position].response}"
        tvTime.text = "Time Taken: ${listResponse[position].time}"
        Glide.with(context)
                .load(listResponse[position].images)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail( 0.1f)
                .into(img)

        return view

    }
}