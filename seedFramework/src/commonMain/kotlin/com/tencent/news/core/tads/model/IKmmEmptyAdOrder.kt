package com.tencent.news.core.tads.model


interface IKmmEmptyAdOrder : IKmmAdOrder

fun IKmmAdOrder?.isEmptyOrder(): Boolean {
    return this is IKmmEmptyAdOrder
}