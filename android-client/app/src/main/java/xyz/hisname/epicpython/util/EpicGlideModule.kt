package xyz.hisname.epicpython.util

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import java.io.InputStream

@GlideModule
class EpicGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val calculator = MemorySizeCalculator.Builder(context)
            .setMemoryCacheScreens(3F)
            .build()
        val memoryCache = calculator.memoryCacheSize.toLong()
        builder.setDiskCache(InternalCacheDiskCacheFactory(context,memoryCache))
        builder.setMemoryCache(LruResourceCache(memoryCache))
        builder.setDefaultRequestOptions(requestOptions(context))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val factory = OkHttpUrlLoader.Factory()
        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }

    companion object {
        private fun requestOptions(context: Context): RequestOptions {
            return RequestOptions()
                .signature(ObjectKey(
                    System.currentTimeMillis() / (24 * 60 * 60 * 1000)))
                .placeholder(IconicsDrawable(context).apply{
                    icon = GoogleMaterial.Icon.gmd_file_download
                    sizeDp = 24
                })
                .error(IconicsDrawable(context).apply{
                    icon = GoogleMaterial.Icon.gmd_error
                    sizeDp = 24
                })
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .encodeQuality(70)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .format(DecodeFormat.PREFER_ARGB_8888)
                .skipMemoryCache(false)
        }
    }
}