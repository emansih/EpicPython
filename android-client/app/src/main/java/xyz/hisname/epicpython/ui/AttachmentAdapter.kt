package xyz.hisname.epicpython.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.hisname.epicpython.databinding.AttachmentItemsBinding
import xyz.hisname.epicpython.model.AttachmentData
import xyz.hisname.epicpython.util.FileUtil

class AttachmentRecyclerAdapter(private val items: MutableList<AttachmentData>):
    RecyclerView.Adapter<AttachmentRecyclerAdapter.AttachmentAdapter>() {

    private lateinit var context: Context
    private var attachmentBinding: AttachmentItemsBinding? = null
    private val binding get() = attachmentBinding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentAdapter {
        context = parent.context
        attachmentBinding = AttachmentItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttachmentAdapter(binding)

    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: AttachmentAdapter, position: Int) = holder.bind(items[position])

    inner class AttachmentAdapter(view: AttachmentItemsBinding): RecyclerView.ViewHolder(view.root) {
        fun bind(attachmentData: AttachmentData){
            val fileName = FileUtil.getFileName(context, attachmentData.attachmentItemUri)
            binding.attachmentName.text = fileName
        }
    }
}