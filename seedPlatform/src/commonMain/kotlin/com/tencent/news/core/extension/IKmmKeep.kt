package com.tencent.news.core.extension


// 标记用接口：主要提供给 Android 使用，标识 kmm 代码中需要防止混淆的类
expect interface IKmmKeep


// 标记用接口：纯血kmm范围内的model类，宿主如果使用Gson解析的话我们会主动抛出异常
// 用这个标记的model类，可以随意使用 @SerialName 注解
interface IKmmPure : IKmmKeep