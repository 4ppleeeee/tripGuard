package com.tencent.news.core.service.api

/**
 * Keeps the historical service-api lookup valid while the concrete factory
 * remains owned by the channel package.
 */
typealias IChannelPageWidgetFactory = com.tencent.news.core.channel.api.IChannelPageWidgetFactory
