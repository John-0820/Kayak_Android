package com.kayakpro.erg.interfaces


interface AppBarCallback {
    fun updateAppBarTitle(title: String, showImage:Boolean, showApp:Boolean, showBottom:Boolean)
    fun updateConnectStatusImage(connectionImage: Int)
}