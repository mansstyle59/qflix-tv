package com.qflix.tv.ui.presenter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.qflix.tv.R
import com.qflix.tv.model.MediaItemQ

class CardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val card = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageScaleType(ImageView.ScaleType.CENTER_CROP)
        }
        card.setMainImageDimensions(313, 176)
        return ViewHolder(card)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val media = item as MediaItemQ
        val card = viewHolder.view as ImageCardView
        card.titleText = media.title
        card.contentText = media.category

        val img = media.posterUrl
        if (!img.isNullOrBlank()) {
            Glide.with(card.context)
                .load(img)
                .placeholder(R.drawable.placeholder_poster)
                .error(R.drawable.placeholder_poster)
                .into(card.mainImageView)
        } else {
            card.mainImageView.setImageResource(R.drawable.placeholder_poster)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val card = viewHolder.view as ImageCardView
        card.mainImageView.setImageDrawable(null)
    }
}
