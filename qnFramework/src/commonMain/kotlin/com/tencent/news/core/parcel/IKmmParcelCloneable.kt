package com.tencent.news.core.parcel


// 基于parcel进行clone
interface IKmmParcelCloneable : IKmmParcelable {

    fun kmmParcelClone(): IKmmParcelable

}