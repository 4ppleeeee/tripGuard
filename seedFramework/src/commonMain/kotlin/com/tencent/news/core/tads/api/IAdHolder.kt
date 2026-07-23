package com.tencent.news.core.tads.api

import com.tencent.news.core.tads.model.IKmmAdFeedsItem
import com.tencent.news.core.tads.model.IKmmAdOrder
import com.tencent.news.core.tads.model.IKmmEmptyAdOrder

import kotlin.js.JsName


interface IAdHolder {

    fun getAllAdOrders(): List<IKmmAdOrder>

    fun addAdOrder(loid: Int, adOrder: IKmmAdOrder?)

    fun getEmptyAdOrders(loid: Int): List<IKmmEmptyAdOrder>

    fun contains(adOrder: IKmmAdOrder?): Boolean

    @JsName("containsWithCondition")
    fun contains(condition: (IKmmAdOrder) -> Boolean): Boolean

    fun findAdOrder(condition: (IKmmAdOrder) -> Boolean): IKmmAdOrder?
    fun findAdItem(condition: (IKmmAdOrder) -> Boolean): IKmmAdFeedsItem?

    fun removeAdItem(adItem: IKmmAdFeedsItem?): Boolean
    fun removeAdOrder(adOrder: IKmmAdOrder?): Boolean

    @JsName("removeAdOrderWithCondition")
    fun removeAdOrder(condition: (IKmmAdOrder) -> Boolean): Boolean

    fun replaceAdItem(oldAdItem: IKmmAdFeedsItem, newAdItem: IKmmAdFeedsItem): Boolean

    fun addAdItem(adOrder: IKmmAdOrder, adItem: IKmmAdFeedsItem)
    
    fun getAdItem(adOrder: IKmmAdOrder): IKmmAdFeedsItem?
    fun getAdItems(loid: Int): List<IKmmAdFeedsItem>
    fun getAllAdItems(): List<IKmmAdFeedsItem>
    fun getAllFeedsAdItems(): List<IKmmAdFeedsItem>
    fun getMidInsertAdItems(): List<IKmmAdFeedsItem>

    fun clearByLoid(loid: Int)
    fun clearAllExcept(loidList: List<Int>)
    fun clearAll()

}