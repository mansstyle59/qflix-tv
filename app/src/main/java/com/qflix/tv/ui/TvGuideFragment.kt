
package com.qflix.tv.ui

import android.content.Intent
import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.qflix.tv.data.ContentStore
import com.qflix.tv.data.EpgRepository
import com.qflix.tv.data.EpgCache
import com.qflix.tv.model.MediaItemQ

class TvGuideFragment: BrowseSupportFragment(){

    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private val cardPresenter = ChannelGuidePresenter()
    private val epgRepo by lazy { EpgRepository(EpgCache(requireContext())) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title="Guide TV"
        headersState=HEADERS_ENABLED
        adapter=rowsAdapter

        buildGuide()

        setOnItemViewClickedListener{_,item,_,_->
            if(item is MediaItemQ){
                startActivity(Intent(requireContext(), PlaybackActivity::class.java).apply{
                    putExtra(DetailsFragment.EXTRA_MEDIA,item)
                })
            }
        }
    }

    private fun buildGuide(){
        rowsAdapter.clear()

        val live=ContentStore.lastLoadedItems.filter{it.isLive}
        val programs=ContentStore.epgPrograms

        val adapter=ArrayObjectAdapter(cardPresenter)

        for(ch in live){
            val key=ch.tvgId ?: ch.title
            val nn=epgRepo.nowNext(programs,key)
            val label=buildString{
                append(ch.title)
                nn.now?.let{append("\nMaintenant: "+it.title)}
                nn.next?.let{append("\nEnsuite: "+it.title)}
            }
            adapter.add(ChannelGuideItem(ch,label))
        }

        rowsAdapter.add(ListRow(HeaderItem(0,"Chaînes"),adapter))
    }

    data class ChannelGuideItem(val media:MediaItemQ,val label:String)

    class ChannelGuidePresenter: Presenter(){
        override fun onCreateViewHolder(parent:android.view.ViewGroup):ViewHolder{
            val tv=android.widget.TextView(parent.context)
            tv.setPadding(20,20,20,20)
            tv.setTextColor(0xFFFFFFFF.toInt())
            tv.textSize=18f
            return ViewHolder(tv)
        }
        override fun onBindViewHolder(vh:ViewHolder,item:Any){
            val i=item as ChannelGuideItem
            (vh.view as android.widget.TextView).text=i.label
            vh.view.tag=i.media
        }
        override fun onUnbindViewHolder(vh:ViewHolder){}
    }
}
